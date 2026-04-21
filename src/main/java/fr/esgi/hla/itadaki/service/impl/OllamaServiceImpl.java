package fr.esgi.hla.itadaki.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.esgi.hla.itadaki.exception.MealAnalysisException;
import fr.esgi.hla.itadaki.service.OllamaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Calls Ollama /api/chat with a base64-encoded image; wraps all failures in MealAnalysisException. */
@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaServiceImpl implements OllamaService {

    private static final String KEY_MODEL       = "model";
    private static final String KEY_MESSAGES    = "messages";
    private static final String KEY_MESSAGE     = "message";
    private static final String KEY_STREAM      = "stream";
    private static final String KEY_FORMAT      = "format";
    private static final String KEY_OPTIONS     = "options";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_NUM_CTX     = "num_ctx";
    private static final String KEY_ROLE        = "role";
    private static final String KEY_CONTENT     = "content";
    private static final String KEY_IMAGES      = "images";
    private static final String ROLE_SYSTEM     = "system";
    private static final String ROLE_USER       = "user";
    private static final String API_CHAT        = "/api/chat";
    private static final String FORMAT_JSON     = "json";

    private static final String SYSTEM_PROMPT = """
            Tu es un nutritionniste analysant la photo d'une assiette.

            Retourne UNIQUEMENT du JSON valide correspondant exactement à ce schéma (pas de texte, pas de markdown, pas de balises) :
            {
              "nomPlat": string,
              "ingredients": [
                {"nom": string, "caloriesApprox": integer, "proteines": number, "glucides": number, "lipides": number},
                ...
              ],
              "portion": string,
              "caloriesMin": integer,
              "caloriesMax": integer,
              "confiance": string
            }

            Règles :
            - JSON uniquement. Aucune explication, aucun commentaire, aucune balise markdown.
            - nomPlat et les champs nom des ingrédients DOIVENT être en français.
            - portion DOIT être en minuscules français : "petit", "moyen", "grand".
            - caloriesMin doit être strictement inférieur à caloriesMax, tous deux entiers positifs.
            - caloriesApprox est en kcal pour la portion détectée de cet ingrédient.
            - proteines, glucides, lipides sont en grammes pour la portion détectée.
            - confiance DOIT être l'un de : "haute", "moyenne", "basse".
            - Par défaut, confiance = "moyenne" si incertain.
            - Si aucun plat n'est visible, retourner :
              {"nomPlat":"inconnu","ingredients":[],"portion":"moyen","caloriesMin":0,"caloriesMax":0,"confiance":"basse"}
            """;

    private static final String DEFAULT_USER_PROMPT =
            "Identifie ce plat, liste les ingrédients visibles et estime les calories.";

    private final RestClient ollamaRestClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.ollama.chat.model}")
    private String model;

    @Value("${spring.ai.ollama.base-url}")
    private String baseUrl;

    @Override
    public String analyzeImage(String imagePath, String prompt) {
        try {
            String base64Image = encodeImageToBase64(imagePath);
            Map<String, Object> body = buildRequestBody(prompt, base64Image);

            log.debug("Calling Ollama {} with model={} for image={}", API_CHAT, model, imagePath);
            OllamaChatResponse response = ollamaRestClient.post()
                    .uri(API_CHAT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(OllamaChatResponse.class);

            return extractContent(response);
        } catch (MealAnalysisException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Ollama analysis failed for image {}: {}", imagePath, ex.getMessage(), ex);
            throw new MealAnalysisException("Ollama analysis failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String chatText(String systemPrompt, String userPrompt, boolean jsonMode) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put(KEY_MODEL, model);
            body.put(KEY_MESSAGES, List.of(
                    Map.of(KEY_ROLE, ROLE_SYSTEM, KEY_CONTENT, systemPrompt),
                    Map.of(KEY_ROLE, ROLE_USER, KEY_CONTENT, userPrompt)
            ));
            body.put(KEY_STREAM, false);
            if (jsonMode) body.put(KEY_FORMAT, FORMAT_JSON);
            body.put(KEY_OPTIONS, Map.of(
                    KEY_TEMPERATURE, jsonMode ? 0.3 : 0.7,
                    KEY_NUM_CTX, 4096
            ));

            log.debug("Calling Ollama {} text-only (jsonMode={})", API_CHAT, jsonMode);
            OllamaChatResponse response = ollamaRestClient.post()
                    .uri(API_CHAT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(OllamaChatResponse.class);

            if (response == null || response.message() == null || response.message().content() == null) {
                throw new MealAnalysisException("Ollama returned empty or malformed response");
            }
            return response.message().content();
        } catch (MealAnalysisException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Ollama chatText failed: {}", ex.getMessage(), ex);
            throw new MealAnalysisException("Ollama chatText failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String streamAnalyzeImage(String imagePath, String prompt, Consumer<String> onToken) {
        try {
            byte[] bytes = Files.readAllBytes(Path.of(imagePath));
            String base64 = Base64.getEncoder().encodeToString(bytes);

            String userContent = prompt == null || prompt.isBlank() ? DEFAULT_USER_PROMPT : prompt;
            Map<String, Object> body = Map.of(
                    KEY_MODEL, model,
                    KEY_MESSAGES, List.of(
                            Map.of(KEY_ROLE, ROLE_SYSTEM, KEY_CONTENT, SYSTEM_PROMPT),
                            Map.of(KEY_ROLE, ROLE_USER, KEY_CONTENT, userContent, KEY_IMAGES, List.of(base64))
                    ),
                    KEY_STREAM, true,
                    KEY_FORMAT, FORMAT_JSON,
                    KEY_OPTIONS, Map.of(KEY_TEMPERATURE, 0.2, KEY_NUM_CTX, 4096)
            );
            String jsonBody = objectMapper.writeValueAsString(body);

            // JDK HttpClient natif : stream=true => Ollama emet une ligne NDJSON par token.
            // On contourne Spring RestClient (pas d'API streaming simple en 2.0-M4).
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + API_CHAT))
                    .timeout(Duration.ofMinutes(10))
                    .header("Content-Type", "application/json")
                    .header("ngrok-skip-browser-warning", "any")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<Stream<String>> response = client.send(req, HttpResponse.BodyHandlers.ofLines());
            if (response.statusCode() >= 400) {
                throw new MealAnalysisException("Ollama returned HTTP " + response.statusCode());
            }

            return collectStreamOutput(response.body(), onToken);

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new MealAnalysisException("Ollama streaming analysis interrupted", ex);
        } catch (MealAnalysisException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Ollama streaming analysis failed for image {}: {}", imagePath, ex.getMessage(), ex);
            throw new MealAnalysisException("Ollama streaming analysis failed: " + ex.getMessage(), ex);
        }
    }

    private String collectStreamOutput(Stream<String> streamLines, Consumer<String> onToken) {
        StringBuilder full = new StringBuilder();
        try (Stream<String> lines = streamLines) {
            lines.forEach(line -> processStreamLine(line, full, onToken));
        }
        return full.toString();
    }

    private void processStreamLine(String line, StringBuilder full, Consumer<String> onToken) {
        if (line.isBlank()) return;
        try {
            JsonNode node = objectMapper.readTree(line);
            JsonNode msg = node.get(KEY_MESSAGE);
            if (msg != null) {
                JsonNode content = msg.get(KEY_CONTENT);
                if (content != null && content.isTextual()) {
                    String token = content.asText();
                    if (!token.isEmpty()) {
                        full.append(token);
                        safeEmitToken(onToken, token);
                    }
                }
            }
        } catch (Exception _) {
            log.debug("Could not parse Ollama stream line: {}", line);
        }
    }

    @Override
    public String buildMealAnalysisPrompt(String hint) {
        if (hint == null || hint.isBlank()) {
            return DEFAULT_USER_PROMPT;
        }
        return """
                L'utilisateur signale que l'analyse précédente est incorrecte :
                <correction>
                %s
                </correction>
                Réanalyse la photo en tenant compte de cette correction. \
                Retourne UNIQUEMENT du JSON valide correspondant au schéma. Aucun texte.
                """.formatted(hint);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void safeEmitToken(Consumer<String> onToken, String token) {
        try {
            onToken.accept(token);
        } catch (Exception _) {
            // do not block the stream
        }
    }

    private String encodeImageToBase64(String imagePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Path.of(imagePath));
        return Base64.getEncoder().encodeToString(bytes);
    }

    private Map<String, Object> buildRequestBody(String prompt, String base64Image) {
        String userPrompt = (prompt == null || prompt.isBlank()) ? DEFAULT_USER_PROMPT : prompt;
        return Map.of(
                KEY_MODEL, model,
                KEY_MESSAGES, List.of(
                        Map.of(KEY_ROLE, ROLE_SYSTEM, KEY_CONTENT, SYSTEM_PROMPT),
                        Map.of(KEY_ROLE, ROLE_USER, KEY_CONTENT, userPrompt, KEY_IMAGES, List.of(base64Image))
                ),
                KEY_STREAM, false,
                KEY_FORMAT, FORMAT_JSON,
                KEY_OPTIONS, Map.of(KEY_TEMPERATURE, 0.2, KEY_NUM_CTX, 4096)
        );
    }

    private String extractContent(OllamaChatResponse response) {
        if (response == null || response.message() == null || response.message().content() == null) {
            throw new MealAnalysisException("Ollama returned empty or malformed response");
        }
        String content = response.message().content();
        log.debug("Ollama response content: {}", content);
        return content;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaChatResponse(OllamaMessage message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaMessage(String role, String content) {}
}

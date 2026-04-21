package fr.esgi.hla.itadaki.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.esgi.hla.itadaki.exception.MealAnalysisException;
import fr.esgi.hla.itadaki.service.OllamaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Implements OllamaService using a plain {@link RestClient} (not Spring AI ChatClient)
 * so the code is decoupled from Spring AI 2.0.0-M4 milestone API changes.
 *
 * Flow for {@link #analyzeImage(String, String)}:
 * <ol>
 *   <li>Read the image bytes from disk and base64-encode them.</li>
 *   <li>Build the Ollama {@code /api/chat} payload with system + user messages,
 *       {@code stream=false}, {@code format=json}, {@code temperature=0.2}.</li>
 *   <li>POST the payload, retrieve {@code message.content} from the response
 *       (which is already a JSON string thanks to {@code format=json}).</li>
 *   <li>Return the raw JSON string — parsing into the domain model happens in
 *       {@code AnalysisServiceImpl}.</li>
 * </ol>
 *
 * All failures are wrapped into a {@link MealAnalysisException} so the
 * {@code GlobalExceptionHandler} can map them to a clean HTTP response.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaServiceImpl implements OllamaService {

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

    @Value("${spring.ai.ollama.chat.model}")
    private String model;

    @Override
    public String analyzeImage(String imagePath, String prompt) {
        try {
            byte[] bytes = Files.readAllBytes(Path.of(imagePath));
            String base64 = Base64.getEncoder().encodeToString(bytes);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of(
                                    "role", "user",
                                    "content", prompt == null || prompt.isBlank() ? DEFAULT_USER_PROMPT : prompt,
                                    "images", List.of(base64)
                            )
                    ),
                    "stream", false,
                    "format", "json",
                    "options", Map.of(
                            "temperature", 0.2,
                            "num_ctx", 4096
                    )
            );

            log.debug("Calling Ollama /api/chat with model={} for image={}", model, imagePath);
            OllamaChatResponse response = ollamaRestClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(OllamaChatResponse.class);

            if (response == null || response.message() == null || response.message().content() == null) {
                throw new MealAnalysisException("Ollama returned empty or malformed response");
            }

            String content = response.message().content();
            log.debug("Ollama response content: {}", content);
            return content;

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
            body.put("model", model);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            body.put("stream", false);
            if (jsonMode) body.put("format", "json");
            body.put("options", Map.of(
                    "temperature", jsonMode ? 0.3 : 0.7,
                    "num_ctx", 4096
            ));

            log.debug("Calling Ollama /api/chat text-only (jsonMode={})", jsonMode);
            OllamaChatResponse response = ollamaRestClient.post()
                    .uri("/api/chat")
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
    public String buildMealAnalysisPrompt(String hint) {
        if (hint == null || hint.isBlank()) {
            return DEFAULT_USER_PROMPT;
        }
        return """
                L'utilisateur indique que l'analyse précédente est incorrecte avec l'indication suivante :
                <user_correction>
                %s
                </user_correction>

                Re-analyse the photo taking this correction into account. \
                Return ONLY valid JSON matching the schema. No prose.
                """.formatted(hint);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaChatResponse(OllamaMessage message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaMessage(String role, String content) {
    }
}

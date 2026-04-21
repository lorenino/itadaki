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

/** Calls Ollama /api/chat with a base64-encoded image; wraps all failures in MealAnalysisException. */
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
            String base64Image = encodeImageToBase64(imagePath);
            Map<String, Object> body = buildRequestBody(prompt, base64Image);

            log.debug("Calling Ollama /api/chat with model={} for image={}", model, imagePath);
            OllamaChatResponse response = ollamaRestClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(OllamaChatResponse.class);

            return extractContent(response, imagePath);
        } catch (MealAnalysisException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Ollama analysis failed for image {}: {}", imagePath, ex.getMessage(), ex);
            throw new MealAnalysisException("Ollama analysis failed: " + ex.getMessage(), ex);
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

    private String encodeImageToBase64(String imagePath) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(imagePath));
        return Base64.getEncoder().encodeToString(bytes);
    }

    private Map<String, Object> buildRequestBody(String prompt, String base64Image) {
        String userPrompt = (prompt == null || prompt.isBlank()) ? DEFAULT_USER_PROMPT : prompt;
        return Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt, "images", List.of(base64Image))
                ),
                "stream", false,
                "format", "json",
                "options", Map.of("temperature", 0.2, "num_ctx", 4096)
        );
    }

    private String extractContent(OllamaChatResponse response, String imagePath) {
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

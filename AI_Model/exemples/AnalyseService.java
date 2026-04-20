package fr.esgi.hla.itadaki.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service Spring AI : analyse initiale + correction 2ᵉ passe via Ollama multimodal.
 *
 * ⚠ Ce fichier est basé sur l'API Spring AI 1.x (la plus documentée).
 * Le pom actuel utilise Spring AI 2.0.0-M4 (milestone) : vérifier les imports / signatures :
 *   - UserMessage.builder().media(Media) peut être .image(Media) ou .images(List<Media>)
 *   - Media constructeur peut être différent
 *   - content() package a pu bouger
 * Si compile échoue, consulter https://docs.spring.io/spring-ai/reference/ pour 2.0-M4.
 */
@Service
@Slf4j
public class AnalyseService {

    private static final String SYSTEM_PROMPT = """
            You are a nutritionist analyzing a photo of a plate/dish.

            Return ONLY valid JSON matching this exact schema (no prose, no markdown, no code fences):
            {
              "nomPlat": string,
              "ingredients": string[],
              "portion": string,
              "caloriesMin": integer,
              "caloriesMax": integer,
              "confiance": string
            }

            Rules:
            - JSON only. No explanation.
            - nomPlat and ingredients MUST be in French.
            - portion ∈ {"petit","moyen","grand"}.
            - confiance ∈ {"haute","moyenne","basse"}.
            - caloriesMin < caloriesMax, positive integers.
            - If no dish: {"nomPlat":"inconnu","ingredients":[],"portion":"moyen","caloriesMin":0,"caloriesMax":0,"confiance":"basse"}.
            """;

    private static final String USER_PROMPT_V1 =
            "Identifie ce plat, liste les ingrédients visibles et estime les calories.";

    private static final String STRICT_RETRY_PROMPT = """
            STRICT MODE: respond with ONLY valid JSON matching the schema.
            No markdown, no code fences, no prose. Your previous reply was invalid.

            Identifie ce plat, liste les ingrédients visibles et estime les calories.
            """;

    private final ChatClient chat;

    public AnalyseService(ChatClient.Builder builder) {
        this.chat = builder.defaultSystem(SYSTEM_PROMPT).build();
    }

    /** Passe initiale : photo → AnalyseResult. Retry 1 fois si JSON invalide. */
    public AnalyseResult analyserInitiale(Path photoPath, String contentType) {
        try {
            byte[] bytes = Files.readAllBytes(photoPath);
            Media media = new Media(MimeTypeUtils.parseMimeType(contentType),
                                    new ByteArrayResource(bytes));

            UserMessage userMsg = UserMessage.builder()
                    .text(USER_PROMPT_V1)
                    .media(media)
                    .build();

            return appelerAvecRetry(userMsg, media);
        } catch (Exception e) {
            log.error("Analyse initiale échouée sur {}", photoPath, e);
            return AnalyseResult.inconnu();
        }
    }

    /** 2ᵉ passe stateless : injecte analyseV1 + correction user dans le prompt. */
    public AnalyseResult corriger(Path photoPath, String contentType,
                                   String analyseV1Json, String correctionUtilisateur) {
        try {
            byte[] bytes = Files.readAllBytes(photoPath);
            Media media = new Media(MimeTypeUtils.parseMimeType(contentType),
                                    new ByteArrayResource(bytes));

            String promptV2 = """
                    You previously produced this analysis of the same photo:
                    <previous_analysis>
                    %s
                    </previous_analysis>

                    The user corrects your analysis with this indication:
                    <user_correction>
                    %s
                    </user_correction>

                    Re-analyze the photo taking the correction into account. Return ONLY valid JSON with the same schema. No prose.
                    """.formatted(analyseV1Json, correctionUtilisateur);

            UserMessage userMsg = UserMessage.builder()
                    .text(promptV2)
                    .media(media)
                    .build();

            return appelerAvecRetry(userMsg, media);
        } catch (Exception e) {
            log.error("Correction échouée sur {}", photoPath, e);
            return AnalyseResult.inconnu();
        }
    }

    private AnalyseResult appelerAvecRetry(UserMessage initialMsg, Media mediaForRetry) {
        try {
            AnalyseResult result = chat.prompt(new Prompt(initialMsg))
                    .call()
                    .entity(AnalyseResult.class);
            if (result.estValide()) {
                return result;
            }
            log.warn("Résultat JSON non valide, retry STRICT MODE");
        } catch (Exception e) {
            log.warn("Parse JSON échoué première tentative : {}", e.getMessage());
        }

        // Retry STRICT MODE
        try {
            UserMessage retry = UserMessage.builder()
                    .text(STRICT_RETRY_PROMPT)
                    .media(mediaForRetry)
                    .build();
            return chat.prompt(new Prompt(retry))
                    .call()
                    .entity(AnalyseResult.class);
        } catch (Exception e2) {
            log.error("Retry STRICT MODE échoué aussi, fallback inconnu", e2);
            return AnalyseResult.inconnu();
        }
    }
}

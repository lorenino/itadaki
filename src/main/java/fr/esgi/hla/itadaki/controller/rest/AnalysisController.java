package fr.esgi.hla.itadaki.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.esgi.hla.itadaki.dto.analysis.MealAnalysisResponseDto;
import fr.esgi.hla.itadaki.dto.analysis.ReanalyzeRequestDto;
import fr.esgi.hla.itadaki.service.AnalysisService;
import fr.esgi.hla.itadaki.service.MealPhotoService;
import fr.esgi.hla.itadaki.service.OllamaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Map;

/**
 * REST controller for meal analysis endpoints.
 * Handles AI analysis result retrieval and re-analysis triggers,
 * plus a streaming NDJSON endpoint that relays Ollama tokens in real time.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analyses")
@Tag(name = "Analyses", description = "AI meal analysis endpoints")
@Slf4j
public class AnalysisController {

    private final AnalysisService analysisService;
    private final OllamaService ollamaService;
    private final MealPhotoService mealPhotoService;
    private final ObjectMapper objectMapper;

    @GetMapping("/{mealId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get analysis for a meal")
    public MealAnalysisResponseDto getAnalysis(@PathVariable Long mealId) {
        return analysisService.getAnalysis(mealId);
    }

    @PostMapping("/{mealId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Trigger or re-analyze a meal")
    public MealAnalysisResponseDto analyzeMeal(
            @PathVariable Long mealId,
            @RequestBody(required = false) @Valid ReanalyzeRequestDto request) {
        if (request != null && request.hint() != null && !request.hint().isBlank()) {
            return analysisService.reanalyzeMeal(mealId, request);
        }
        return analysisService.analyzeMeal(mealId);
    }

    /**
     * Reçoit les tokens Ollama en temps réel, ligne par ligne (NDJSON).
     * Chaque ligne : {"type":"token","content":"..."}, {"type":"complete",...} ou {"type":"error",...}
     * Utilise un Thread séparé pour envoyer sans bloquer le serveur.
     */
    @PostMapping(value = "/stream/{mealId}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Stream Ollama tokens in real time for a meal analysis")
    public ResponseBodyEmitter streamAnalyze(
            @PathVariable Long mealId,
            @RequestBody(required = false) @Valid ReanalyzeRequestDto request) {
        final String hint = request != null ? request.hint() : null;
        // 10 min max pour couvrir cold start + generation longue
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(600_000L);

        Thread worker = new Thread(() -> {
            try {
                analysisService.markAnalysing(mealId);
                String imagePath = mealPhotoService.getStoredPath(mealId);
                String prompt = ollamaService.buildMealAnalysisPrompt(hint);

                String full = ollamaService.streamAnalyzeImage(imagePath, prompt, token -> {
                    try {
                        emitter.send(toJsonLine(Map.of("type", "token", "content", token)), MediaType.APPLICATION_JSON);
                    } catch (Exception _) { /* client parti, on laisse le thread finir proprement */ }
                });

                MealAnalysisResponseDto dto = analysisService.persistStreamResult(mealId, full);
                emitter.send(toJsonLine(Map.of("type", "complete", "analysis", dto)), MediaType.APPLICATION_JSON);
                emitter.complete();
            } catch (Exception ex) {
                log.error("Streaming analysis failed for meal {}: {}", mealId, ex.getMessage(), ex);
                try { analysisService.markFailed(mealId); } catch (Exception _) { /* best effort */ }
                try {
                    emitter.send(toJsonLine(Map.of("type", "error", "message", ex.getMessage() != null ? ex.getMessage() : "analysis failed")), MediaType.APPLICATION_JSON);
                } catch (Exception _) { /* best effort */ }
                emitter.completeWithError(ex);
            }
        }, "ollama-stream-" + mealId);
        worker.setDaemon(true);
        worker.start();

        return emitter;
    }

    private String toJsonLine(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload) + "\n";
        } catch (Exception _) {
            return "{\"type\":\"error\",\"message\":\"serialization failed\"}\n";
        }
    }
}

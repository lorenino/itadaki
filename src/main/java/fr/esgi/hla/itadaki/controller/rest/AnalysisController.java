package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.service.AnalysisService;
import fr.esgi.hla.itadaki.service.OllamaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle meal analysis endpoints.
 *       - GET  api/analyses/{mealId}  → get analysis result for a meal
 *       - POST api/analyses/{mealId}  → trigger or re-trigger AI analysis (uses photo path)
 *       Inject AnalysisService and OllamaService.
 *       Return MealAnalysisResponseDto.
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/analyses")
@Tag(name = "Analyses", description = "AI meal analysis endpoints")
public class AnalysisController {

    private AnalysisService analysisService;
    private OllamaService ollamaService;

    // TODO: @Operation(summary = "Get analysis for a meal") GET /{mealId} → MealAnalysisResponseDto
    // TODO: @Operation(summary = "Trigger AI re-analysis for a meal") POST /{mealId} → MealAnalysisResponseDto
}

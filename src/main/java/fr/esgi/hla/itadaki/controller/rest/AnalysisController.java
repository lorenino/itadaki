package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.dto.analysis.MealAnalysisResponseDto;
import fr.esgi.hla.itadaki.dto.analysis.ReanalyzeRequestDto;
import fr.esgi.hla.itadaki.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for meal analysis endpoints.
 * Handles AI analysis result retrieval and re-analysis triggers.
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/analyses")
@Tag(name = "Analyses", description = "AI meal analysis endpoints")
public class AnalysisController {

    private AnalysisService analysisService;

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
}

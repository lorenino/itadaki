package fr.esgi.hla.itadaki.controller.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle meal analysis endpoints.
 *       - GET  /api/analyses/{mealId}    → get analysis result for a meal
 *       - POST /api/analyses/{mealId}    → trigger or re-trigger AI analysis
 *       Inject AnalysisService and OllamaService.
 *       Return MealAnalysisResponseDto.
 */
@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {
    // TODO: Inject AnalysisService
    // TODO: Implement GET analysis endpoint
    // TODO: Implement POST re-analyse endpoint
}

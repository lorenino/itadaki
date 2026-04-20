package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.service.CorrectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle user correction endpoints.
 *       - POST api/corrections/{mealId} → submit a manual correction for a meal analysis
 *       - GET  api/corrections/{mealId} → get the correction for a meal
 *       Inject CorrectionService.
 *       Accept MealCorrectionRequestDto and return MealCorrectionResponseDto.
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/corrections")
@Tag(name = "Corrections", description = "Manual meal analysis correction endpoints")
public class CorrectionController {

    private CorrectionService correctionService;

    // TODO: @Operation(summary = "Submit a correction for a meal analysis") POST /{mealId} → MealCorrectionResponseDto
    // TODO: @Operation(summary = "Get the correction for a meal") GET /{mealId} → MealCorrectionResponseDto
}

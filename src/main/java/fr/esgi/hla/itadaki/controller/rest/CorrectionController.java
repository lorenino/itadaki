package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.dto.correction.MealCorrectionRequestDto;
import fr.esgi.hla.itadaki.dto.correction.MealCorrectionResponseDto;
import fr.esgi.hla.itadaki.service.CorrectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for meal correction endpoints.
 * Handles manual analysis corrections submitted by users.
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/corrections")
@Tag(name = "Corrections", description = "Manual meal analysis correction endpoints")
public class CorrectionController {

    private CorrectionService correctionService;

    @PostMapping("/{mealId}")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Submit a correction for a meal analysis")
    public MealCorrectionResponseDto submitCorrection(
            @PathVariable Long mealId,
            @RequestBody @Valid MealCorrectionRequestDto request) {
        return correctionService.submitCorrection(mealId, request);
    }

    @GetMapping("/{mealId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the correction for a meal")
    public MealCorrectionResponseDto getCorrection(@PathVariable Long mealId) {
        return correctionService.getCorrection(mealId);
    }
}

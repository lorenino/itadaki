package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.correction.MealCorrectionRequestDto;
import fr.esgi.hla.itadaki.dto.correction.MealCorrectionResponseDto;

/**
 * Service interface for meal correction operations.
 * - submitCorrection(Long mealId, MealCorrectionRequestDto) → MealCorrectionResponseDto
 * - getCorrection(Long mealId)                              → MealCorrectionResponseDto
 */
public interface CorrectionService {

    MealCorrectionResponseDto submitCorrection(Long mealId, MealCorrectionRequestDto request);

    MealCorrectionResponseDto getCorrection(Long mealId);
}

package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.correction.MealCorrectionRequestDto;
import fr.esgi.hla.itadaki.dto.correction.MealCorrectionResponseDto;

/** Submits and retrieves user corrections for meal analysis results. */
public interface CorrectionService {

    MealCorrectionResponseDto submitCorrection(Long mealId, MealCorrectionRequestDto request);

    MealCorrectionResponseDto getCorrection(Long mealId);
}

package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.analysis.MealAnalysisResponseDto;
import fr.esgi.hla.itadaki.dto.analysis.ReanalyzeRequestDto;

/**
 * Service interface for AI meal analysis operations.
 * - analyzeMeal(Long mealId)                             → MealAnalysisResponseDto
 * - reanalyzeMeal(Long mealId, ReanalyzeRequestDto hint) → MealAnalysisResponseDto
 * - getAnalysis(Long mealId)                             → MealAnalysisResponseDto
 */
public interface AnalysisService {

    MealAnalysisResponseDto analyzeMeal(Long mealId);

    MealAnalysisResponseDto reanalyzeMeal(Long mealId, ReanalyzeRequestDto request);

    MealAnalysisResponseDto getAnalysis(Long mealId);
}

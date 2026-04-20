package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TODO: Implements AnalysisService.
 *       - analyzeMeal: update meal status to ANALYSING, resolve photo path via MealPhotoService,
 *                      call OllamaService, parse response, create MealAnalysis, update status to ANALYSED
 *       - reanalyzeMeal: delete existing analysis if any, re-run analyzeMeal
 *       - getAnalysis: fetch MealAnalysis by mealId, map to MealAnalysisResponseDto
 *
 *       Inject: MealRepository, MealAnalysisRepository, OllamaService,
 *               MealPhotoService, MealAnalysisMapper
 */
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    // TODO: Inject MealRepository
    // TODO: Inject MealAnalysisRepository
    // TODO: Inject OllamaService
    // TODO: Inject MealAnalysisMapper

    // TODO: Override analyzeMeal(Long mealId) → MealAnalysisResponseDto
    // TODO: Override reanalyzeMeal(Long mealId, ReanalyzeRequestDto request) → MealAnalysisResponseDto
    // TODO: Override getAnalysis(Long mealId) → MealAnalysisResponseDto
}

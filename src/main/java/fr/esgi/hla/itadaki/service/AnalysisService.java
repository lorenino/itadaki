package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.analysis.MealAnalysisResponseDto;
import fr.esgi.hla.itadaki.dto.analysis.ReanalyzeRequestDto;

/**
 * Service interface for AI meal analysis operations.
 */
public interface AnalysisService {

    MealAnalysisResponseDto analyzeMeal(Long mealId);

    MealAnalysisResponseDto reanalyzeMeal(Long mealId, ReanalyzeRequestDto request);

    MealAnalysisResponseDto getAnalysis(Long mealId);

    /** Marque le meal en ANALYSING, utilise par le flow streaming avant appel LLM. */
    void markAnalysing(Long mealId);

    /** Marque le meal en FAILED, utilise si le stream LLM crashe. */
    void markFailed(Long mealId);

    /**
     * Persiste le resultat d'une analyse streamee : parse le JSON complet,
     * UPSERT l'analyse, passe le meal en ANALYSED, retourne le DTO pret a emit.
     */
    MealAnalysisResponseDto persistStreamResult(Long mealId, String rawJson);
}

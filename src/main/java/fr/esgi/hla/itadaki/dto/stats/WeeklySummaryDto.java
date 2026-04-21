package fr.esgi.hla.itadaki.dto.stats;

import java.io.Serializable;

/**
 * Bilan hebdomadaire narratif genere par le LLM Ollama.
 * Prend en entree les stats 7 derniers jours + macros, produit un paragraphe
 * conversationnel ("Cette semaine tu as consomme..."). Cache cote front.
 */
public record WeeklySummaryDto(
        String summary,
        String periodFrom,
        String periodTo,
        int mealCount,
        Double totalCalories,
        String bestDay,
        Double bestDayCalories
) implements Serializable {
}

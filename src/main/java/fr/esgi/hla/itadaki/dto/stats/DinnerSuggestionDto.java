package fr.esgi.hla.itadaki.dto.stats;

import java.io.Serializable;

/**
 * Recommandation de plat generee par le LLM a partir de l'historique
 * nutritionnel de l'utilisateur (7 derniers jours) et de l'heure courante.
 */
public record DinnerSuggestionDto(
        String dishName,
        String reason,
        Integer estimatedCalories,
        String mealType
) implements Serializable {
}

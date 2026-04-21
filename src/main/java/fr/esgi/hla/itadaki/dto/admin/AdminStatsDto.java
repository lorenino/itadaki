package fr.esgi.hla.itadaki.dto.admin;

import java.io.Serializable;

/**
 * DTO admin : metriques globales de l'application.
 */
public record AdminStatsDto(
        long totalUsers,
        long totalMeals,
        long totalAnalyses,
        Double avgCaloriesPerMeal
) implements Serializable {
}

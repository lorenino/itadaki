package fr.esgi.hla.itadaki.dto.stats;

import java.io.Serializable;

/** DTO for the overall nutritional statistics overview of a user. */
public record StatsOverviewDto(
        int totalMeals,
        Double totalCalories,
        Double avgDailyCalories,
        Double avgProtein,
        Double avgCarbs,
        Double avgFat,
        String periodFrom,
        String periodTo
) implements Serializable {
}

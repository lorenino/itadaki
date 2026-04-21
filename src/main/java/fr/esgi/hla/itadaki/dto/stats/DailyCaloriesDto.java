package fr.esgi.hla.itadaki.dto.stats;

import java.io.Serializable;

/** DTO representing calorie intake for a single day. */
public record DailyCaloriesDto(
        String date,
        Double totalCalories,
        int mealCount
) implements Serializable {
}

package fr.esgi.hla.itadaki.dto.meal;

import java.io.Serializable;

/** DTO representing a single food item detected by the AI during meal analysis. */
public record DetectedFoodItemDto(
        String name,
        Double quantity,
        String unit,
        Double calories,
        Double protein,
        Double carbs,
        Double fat
) implements Serializable {
}

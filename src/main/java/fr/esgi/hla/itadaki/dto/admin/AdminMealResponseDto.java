package fr.esgi.hla.itadaki.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.esgi.hla.itadaki.business.enums.MealStatus;
import fr.esgi.hla.itadaki.business.enums.MealType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO admin : repas enrichi du nom de son proprietaire et des calories estimees.
 */
public record AdminMealResponseDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
        MealStatus status,
        MealType mealType,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) LocalDateTime uploadedAt,
        String userName,
        Double calories
) implements Serializable {
}

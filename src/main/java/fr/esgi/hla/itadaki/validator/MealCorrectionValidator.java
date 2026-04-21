package fr.esgi.hla.itadaki.validator;

import fr.esgi.hla.itadaki.dto.correction.MealCorrectionRequestDto;
import fr.esgi.hla.itadaki.dto.correction.CorrectedFoodItemDto;
import fr.esgi.hla.itadaki.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

/** Validates business rules for meal correction requests. */
@Component
public class MealCorrectionValidator {

    public void validate(MealCorrectionRequestDto request) {
        if (request == null) {
            throw new ResourceNotFoundException("Correction request must not be null");
        }

        if (request.correctedItems() == null || request.correctedItems().isEmpty()) {
            throw new ResourceNotFoundException("Corrected items list must not be empty");
        }

        for (CorrectedFoodItemDto item : request.correctedItems()) {
            validateFoodItem(item);
        }

        if (request.correctedTotalCalories() != null && request.correctedTotalCalories() < 0) {
            throw new ResourceNotFoundException("Corrected total calories must not be negative");
        }
    }

    private void validateFoodItem(CorrectedFoodItemDto item) {
        if (item == null) {
            throw new ResourceNotFoundException("Food item must not be null");
        }

        if (item.name() == null || item.name().trim().isEmpty()) {
            throw new ResourceNotFoundException("Food item name must not be blank");
        }

        if (item.calories() != null && item.calories() < 0) {
            throw new ResourceNotFoundException("Food item calories must not be negative");
        }

        if (item.quantity() != null && item.quantity() < 0) {
            throw new ResourceNotFoundException("Food item quantity must not be negative");
        }
    }
}

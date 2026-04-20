package fr.esgi.hla.itadaki.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.business.MealCorrection;
import fr.esgi.hla.itadaki.business.enums.MealStatus;
import fr.esgi.hla.itadaki.dto.correction.CorrectedFoodItemDto;
import fr.esgi.hla.itadaki.dto.correction.MealCorrectionRequestDto;
import fr.esgi.hla.itadaki.dto.correction.MealCorrectionResponseDto;
import fr.esgi.hla.itadaki.exception.ResourceNotFoundException;
import fr.esgi.hla.itadaki.mapper.MealCorrectionMapper;
import fr.esgi.hla.itadaki.repository.MealCorrectionRepository;
import fr.esgi.hla.itadaki.repository.MealRepository;
import fr.esgi.hla.itadaki.service.CorrectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of CorrectionService.
 * Handles user corrections to meal analysis results.
 */
@Service
@RequiredArgsConstructor
public class CorrectionServiceImpl implements CorrectionService {

    private final MealRepository mealRepository;
    private final MealCorrectionRepository mealCorrectionRepository;
    private final MealCorrectionMapper mealCorrectionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public MealCorrectionResponseDto submitCorrection(Long mealId, MealCorrectionRequestDto request) {
        // Verify meal exists
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + mealId));

        // Delete existing correction if any
        mealCorrectionRepository.deleteByMealId(mealId);

        // Create or update MealCorrection entity
        MealCorrection correction = new MealCorrection();
        correction.setMeal(meal);
        correction.setCorrectedDishName(request.correctedDishName());
        correction.setCorrectedTotalCalories(request.correctedTotalCalories());
        correction.setUserComment(request.userComment());

        // Serialize corrected items to JSON
        try {
            String correctedItemsJson = objectMapper.writeValueAsString(request.correctedItems());
            correction.setCorrectedItemsJson(correctedItemsJson);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to serialize correction items: " + ex.getMessage(), ex);
        }

        correction = mealCorrectionRepository.save(correction);

        // Update meal status to CORRECTED
        meal.setStatus(MealStatus.CORRECTED);
        mealRepository.save(meal);

        // Map to DTO and enrich with parsed items
        MealCorrectionResponseDto dto = mealCorrectionMapper.toDto(correction);
        List<CorrectedFoodItemDto> correctedItems = request.correctedItems() != null ?
                new ArrayList<>(request.correctedItems()) : new ArrayList<>();

        return new MealCorrectionResponseDto(
                dto.id(),
                dto.mealId(),
                dto.correctedDishName(),
                correctedItems,
                dto.correctedTotalCalories(),
                dto.userComment(),
                dto.correctedAt()
        );
    }

    @Override
    public MealCorrectionResponseDto getCorrection(Long mealId) {
        MealCorrection correction = mealCorrectionRepository.findByMealId(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Correction not found for meal id: " + mealId));

        MealCorrectionResponseDto dto = mealCorrectionMapper.toDto(correction);
        List<CorrectedFoodItemDto> correctedItems = parseCorrectedItems(correction.getCorrectedItemsJson());

        return new MealCorrectionResponseDto(
                dto.id(),
                dto.mealId(),
                dto.correctedDishName(),
                correctedItems,
                dto.correctedTotalCalories(),
                dto.userComment(),
                dto.correctedAt()
        );
    }

    private List<CorrectedFoodItemDto> parseCorrectedItems(String jsonString) {
        List<CorrectedFoodItemDto> items = new ArrayList<>();
        try {
            if (jsonString != null && !jsonString.isBlank()) {
                CorrectedFoodItemDto[] itemArray = objectMapper.readValue(jsonString, CorrectedFoodItemDto[].class);
                for (CorrectedFoodItemDto item : itemArray) {
                    items.add(item);
                }
            }
        } catch (Exception ex) {
            // If parsing fails, return empty list
        }
        return items;
    }
}

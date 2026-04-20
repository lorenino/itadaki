package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.business.enums.MealType;
import fr.esgi.hla.itadaki.dto.meal.MealResponseDto;
import fr.esgi.hla.itadaki.dto.meal.MealUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for meal management operations.
 * - uploadMeal(MultipartFile image, Long userId) → MealUploadResponseDto
 * - findById(Long id)                            → MealResponseDto
 * - deleteMeal(Long id, Long requestingUserId)   → void
 * - updateMealType(Long id, MealType type, Long userId) → MealResponseDto
 */
public interface MealService {

    MealUploadResponseDto uploadMeal(MultipartFile image, Long userId);

    MealResponseDto findById(Long id);

    void deleteMeal(Long id, Long requestingUserId);

    MealResponseDto updateMealType(Long id, MealType mealType, Long requestingUserId);
}

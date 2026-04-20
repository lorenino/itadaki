package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.meal.MealPhotoResponseDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO: Defines meal photo storage and retrieval operations.
 *       - storePhoto(MultipartFile file, Long mealId) → MealPhotoResponseDto
 *         Stores file via FileStorageService, creates MealPhoto entity linked to meal.
 *       - findByMealId(Long mealId)                   → MealPhotoResponseDto
 *       - deleteByMealId(Long mealId)                  → void
 *         Removes file from filesystem and deletes MealPhoto entity.
 *       - getStoredPath(Long mealId)                   → String
 *         Returns the stored filesystem path for Ollama analysis.
 */
public interface MealPhotoService {

    // TODO: MealPhotoResponseDto storePhoto(MultipartFile file, Long mealId);
    // TODO: MealPhotoResponseDto findByMealId(Long mealId);
    // TODO: void deleteByMealId(Long mealId);
    // TODO: String getStoredPath(Long mealId);
}

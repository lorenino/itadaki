package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.meal.MealPhotoResponseDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for meal photo storage and retrieval operations.
 * - storePhoto(MultipartFile file, Long mealId) → MealPhotoResponseDto
 *   Stores file via FileStorageService, creates MealPhoto entity linked to meal.
 * - findByMealId(Long mealId)                   → MealPhotoResponseDto
 * - deleteByMealId(Long mealId)                  → void
 *   Removes file from filesystem and deletes MealPhoto entity.
 * - getStoredPath(Long mealId)                   → String
 *   Returns the stored filesystem path for Ollama analysis.
 */
public interface MealPhotoService {

    MealPhotoResponseDto storePhoto(MultipartFile file, Long mealId);

    MealPhotoResponseDto findByMealId(Long mealId);

    void deleteByMealId(Long mealId);

    String getStoredPath(Long mealId);
}

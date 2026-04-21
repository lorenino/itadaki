package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.meal.MealPhotoResponseDto;
import org.springframework.web.multipart.MultipartFile;

/** Stores, retrieves, and deletes meal photo files; provides the stored path for Ollama image analysis. */
public interface MealPhotoService {

    MealPhotoResponseDto storePhoto(MultipartFile file, Long mealId);

    MealPhotoResponseDto findByMealId(Long mealId);

    void deleteByMealId(Long mealId);

    String getStoredPath(Long mealId);
}

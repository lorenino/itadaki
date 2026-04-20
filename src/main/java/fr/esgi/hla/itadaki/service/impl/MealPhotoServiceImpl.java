package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.MealPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TODO: Implements MealPhotoService.
 *       - storePhoto: call FileStorageService.store(), create MealPhoto entity
 *                     (originalFilename, storedPath, mimeType, fileSizeBytes), link to Meal
 *       - findByMealId: fetch MealPhoto by meal ID, map to MealPhotoResponseDto
 *       - deleteByMealId: load MealPhoto, call FileStorageService.delete(storedPath), delete entity
 *       - getStoredPath: fetch MealPhoto by meal ID, return storedPath
 *
 *       Inject: MealPhotoRepository, MealRepository, FileStorageService, MealPhotoMapper
 */
@Service
@RequiredArgsConstructor
public class MealPhotoServiceImpl implements MealPhotoService {

    // TODO: Inject MealPhotoRepository
    // TODO: Inject MealRepository
    // TODO: Inject FileStorageService
    // TODO: Inject MealPhotoMapper

    // TODO: Override storePhoto(MultipartFile file, Long mealId) → MealPhotoResponseDto
    // TODO: Override findByMealId(Long mealId) → MealPhotoResponseDto
    // TODO: Override deleteByMealId(Long mealId) → void
    // TODO: Override getStoredPath(Long mealId) → String
}

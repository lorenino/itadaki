package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.business.MealPhoto;
import fr.esgi.hla.itadaki.dto.meal.MealPhotoResponseDto;
import fr.esgi.hla.itadaki.exception.ResourceNotFoundException;
import fr.esgi.hla.itadaki.mapper.MealPhotoMapper;
import fr.esgi.hla.itadaki.repository.MealPhotoRepository;
import fr.esgi.hla.itadaki.repository.MealRepository;
import fr.esgi.hla.itadaki.service.FileStorageService;
import fr.esgi.hla.itadaki.service.MealPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

/**
 * Implementation of MealPhotoService.
 * Handles photo storage, retrieval, and deletion with filesystem integration.
 */
@Service
@RequiredArgsConstructor
public class MealPhotoServiceImpl implements MealPhotoService {

    private final MealPhotoRepository mealPhotoRepository;
    private final MealRepository mealRepository;
    private final FileStorageService fileStorageService;
    private final MealPhotoMapper mealPhotoMapper;

    @Override
    public MealPhotoResponseDto storePhoto(MultipartFile file, Long mealId) {
        // Verify meal exists
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + mealId));

        // Store file via FileStorageService
        String storagePath = fileStorageService.store(file);

        // Create MealPhoto entity
        MealPhoto photo = new MealPhoto();
        photo.setMeal(meal);
        photo.setOriginalFileName(file.getOriginalFilename());
        photo.setFileName(Paths.get(storagePath).getFileName().toString());
        photo.setStoragePath(storagePath);
        photo.setContentType(file.getContentType());
        photo.setSize(file.getSize());

        photo = mealPhotoRepository.save(photo);

        return mealPhotoMapper.toDto(photo);
    }

    @Override
    public MealPhotoResponseDto findByMealId(Long mealId) {
        MealPhoto photo = mealPhotoRepository.findByMealId(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found for meal id: " + mealId));
        return mealPhotoMapper.toDto(photo);
    }

    @Override
    public void deleteByMealId(Long mealId) {
        MealPhoto photo = mealPhotoRepository.findByMealId(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found for meal id: " + mealId));

        // Delete file from filesystem
        fileStorageService.delete(photo.getStoragePath());

        // Delete entity
        mealPhotoRepository.deleteByMealId(mealId);
    }

    @Override
    public String getStoredPath(Long mealId) {
        MealPhoto photo = mealPhotoRepository.findByMealId(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found for meal id: " + mealId));
        return photo.getStoragePath();
    }

}

package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.business.enums.MealStatus;
import fr.esgi.hla.itadaki.dto.meal.MealResponseDto;
import fr.esgi.hla.itadaki.dto.meal.MealUploadResponseDto;
import fr.esgi.hla.itadaki.exception.ResourceNotFoundException;
import fr.esgi.hla.itadaki.exception.UnauthorizedException;
import fr.esgi.hla.itadaki.mapper.MealMapper;
import fr.esgi.hla.itadaki.repository.MealRepository;
import fr.esgi.hla.itadaki.repository.UserRepository;
import fr.esgi.hla.itadaki.service.MealPhotoService;
import fr.esgi.hla.itadaki.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of MealService.
 * Handles meal creation, retrieval, and deletion with ownership validation.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;
    private final UserRepository userRepository;
    private final MealPhotoService mealPhotoService;
    private final MealMapper mealMapper;

    @Override
    public MealUploadResponseDto uploadMeal(MultipartFile image, Long userId) {
        // Verify user exists
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Create meal entity with PENDING status
        Meal meal = new Meal();
        meal.setUser(user);
        meal.setStatus(MealStatus.PENDING);
        meal = mealRepository.save(meal);

        // Store photo via MealPhotoService
        mealPhotoService.storePhoto(image, meal.getId());

        // Return upload response
        return mealMapper.toUploadResponseDto(meal);
    }

    @Override
    public MealResponseDto findById(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + id));
        return mealMapper.toDto(meal);
    }

    @Override
    public void deleteMeal(Long id, Long requestingUserId) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + id));

        // Check ownership
        if (!meal.getUser().getId().equals(requestingUserId)) {
            throw new UnauthorizedException("You do not have permission to delete this meal");
        }

        // Delete photo via MealPhotoService
        mealPhotoService.deleteByMealId(id);

        // Delete meal
        mealRepository.delete(meal);
    }
}

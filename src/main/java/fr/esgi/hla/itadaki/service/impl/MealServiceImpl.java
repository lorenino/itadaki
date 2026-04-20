package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.MealService;
import org.springframework.stereotype.Service;

/**
 * TODO: Implements MealService.
 *       - uploadMeal: validate image, store via FileStorageService,
 *                     create Meal entity (status=PENDING), trigger async analysis
 *       - findById: fetch Meal, check ownership, map to MealResponseDto
 *       - deleteMeal: check ownership or admin role, delete image file, delete entity
 *
 *       Inject: MealRepository, UserRepository, FileStorageService, MealMapper
 */
@Service
public class MealServiceImpl implements MealService {

    // TODO: Inject MealRepository
    // TODO: Inject UserRepository
    // TODO: Inject FileStorageService
    // TODO: Inject MealMapper

    // TODO: Override uploadMeal(MultipartFile image, Long userId) → MealUploadResponseDto
    // TODO: Override findById(Long id) → MealResponseDto
    // TODO: Override deleteMeal(Long id, Long requestingUserId) → void
}

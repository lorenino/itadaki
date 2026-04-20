package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TODO: Implements MealService.
 *       - uploadMeal: validate image, create Meal entity (status=PENDING),
 *                     delegate file storage to MealPhotoService, trigger async analysis
 *       - findById: fetch Meal (with photo), check ownership, map to MealResponseDto
 *       - deleteMeal: check ownership or admin role, delete photo via MealPhotoService, delete entity
 *
 *       Inject: MealRepository, UserRepository, MealPhotoService, MealMapper
 */
@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    // TODO: Inject MealRepository
    // TODO: Inject UserRepository
    // TODO: Inject FileStorageService
    // TODO: Inject MealMapper

    // TODO: Override uploadMeal(MultipartFile image, Long userId) → MealUploadResponseDto
    // TODO: Override findById(Long id) → MealResponseDto
    // TODO: Override deleteMeal(Long id, Long requestingUserId) → void
}

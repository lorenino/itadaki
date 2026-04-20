package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.service.MealService;
import fr.esgi.hla.itadaki.service.MealPhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle meal upload and retrieval endpoints.
 *       - POST   api/meals        → upload a meal image (multipart/form-data); stores via MealPhotoService
 *       - GET    api/meals/{id}   → get a single meal by ID (includes photo info)
 *       - DELETE api/meals/{id}   → delete a meal and its photo (owner or admin)
 *       Use @ValidImageFile on the multipart parameter.
 *       Inject MealService and MealPhotoService; return MealResponseDto / MealUploadResponseDto.
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/meals")
@Tag(name = "Meals", description = "Meal upload and retrieval endpoints")
public class MealController {

    private MealService mealService;
    private MealPhotoService mealPhotoService;

    // TODO: @Operation(summary = "Upload a meal photo") POST / → multipart file → MealUploadResponseDto
    // TODO: @Operation(summary = "Get a meal by ID") GET /{id} → MealResponseDto
    // TODO: @Operation(summary = "Delete a meal") DELETE /{id} → 204 No Content
}

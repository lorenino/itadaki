package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.annotation.CurrentUser;
import fr.esgi.hla.itadaki.annotation.ValidImageFile;
import fr.esgi.hla.itadaki.dto.meal.MealResponseDto;
import fr.esgi.hla.itadaki.dto.meal.MealUploadResponseDto;
import fr.esgi.hla.itadaki.service.MealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for meal management endpoints.
 * Handles meal upload, retrieval, and deletion.
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/meals")
@Tag(name = "Meals", description = "Meal upload and retrieval endpoints")
public class MealController {

    private MealService mealService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload a meal photo")
    public MealUploadResponseDto uploadMeal(
            @RequestParam("image") @ValidImageFile MultipartFile image,
            @CurrentUser Long userId) {
        return mealService.uploadMeal(image, userId);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get a meal by ID")
    public MealResponseDto getMealById(@PathVariable Long id) {
        return mealService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a meal")
    public void deleteMeal(@PathVariable Long id, @CurrentUser Long userId) {
        mealService.deleteMeal(id, userId);
    }
}

package fr.esgi.hla.itadaki.controller.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle meal upload and retrieval endpoints.
 *       - POST /api/meals          → upload a meal image (multipart/form-data)
 *       - GET  /api/meals/{id}     → get a single meal by ID
 *       - DELETE /api/meals/{id}   → delete a meal (owner or admin)
 *       Use @ValidImageFile on the multipart parameter.
 *       Inject MealService and return MealResponseDto / MealUploadResponseDto.
 */
@RestController
@RequestMapping("/api/meals")
public class MealController {
    // TODO: Inject MealService
    // TODO: Implement POST upload endpoint
    // TODO: Implement GET single meal endpoint
    // TODO: Implement DELETE meal endpoint
}

package fr.esgi.hla.itadaki.controller.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle user meal history endpoints.
 *       - GET /api/history         → paginated list of the authenticated user's meals
 *       - GET /api/history/{date}  → meals for a specific date
 *       Inject HistoryService.
 *       Return List<MealHistoryItemDto> with pagination support.
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {
    // TODO: Inject HistoryService
    // TODO: Implement GET history endpoint with pagination
    // TODO: Implement GET history by date endpoint
}

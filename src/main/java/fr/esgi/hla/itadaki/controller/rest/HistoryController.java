package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle user meal history endpoints.
 *       - GET api/history         → paginated list of the authenticated user's meals
 *       - GET api/history/{date}  → meals for a specific date
 *       Inject HistoryService.
 *       Return List<MealHistoryItemDto> with pagination support.
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/history")
@Tag(name = "History", description = "Meal history and date-based retrieval endpoints")
public class HistoryController {

    private HistoryService historyService;

    // TODO: @Operation(summary = "Get paginated meal history for authenticated user") GET / → Page<MealHistoryItemDto>
    // TODO: @Operation(summary = "Get meals for a specific date") GET /{date} → List<MealHistoryItemDto>
}

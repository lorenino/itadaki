package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.annotation.CurrentUser;
import fr.esgi.hla.itadaki.dto.meal.MealHistoryItemDto;
import fr.esgi.hla.itadaki.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for meal history endpoints.
 * Provides paginated and date-based meal history retrieval for authenticated users.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/history")
@Tag(name = "History", description = "Meal history and date-based retrieval endpoints")
public class HistoryController {

    private HistoryService historyService;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get paginated meal history for authenticated user")
    public Page<MealHistoryItemDto> getHistory(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return historyService.getHistory(userId, pageable);
    }

    @GetMapping("/date/{date}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get meals for a specific date")
    public List<MealHistoryItemDto> getHistoryByDate(
            @CurrentUser Long userId,
            @PathVariable LocalDate date) {
        return historyService.getHistoryByDate(userId, date);
    }
}

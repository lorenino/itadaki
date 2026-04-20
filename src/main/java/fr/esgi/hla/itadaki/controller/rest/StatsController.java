package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.annotation.CurrentUser;
import fr.esgi.hla.itadaki.dto.stats.DailyCaloriesDto;
import fr.esgi.hla.itadaki.dto.stats.StatsOverviewDto;
import fr.esgi.hla.itadaki.dto.stats.StreakDto;
import fr.esgi.hla.itadaki.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for nutrition statistics endpoints.
 * Provides aggregated calorie and nutrition data for authenticated users.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/stats")
@Tag(name = "Statistics", description = "Nutritional statistics endpoints")
public class StatsController {

    private StatsService statsService;

    @GetMapping("/overview")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get overall nutrition stats for authenticated user")
    public StatsOverviewDto getOverview(@CurrentUser Long userId) {
        return statsService.getOverview(userId);
    }

    @GetMapping("/daily")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get daily calorie breakdown")
    public List<DailyCaloriesDto> getDailyCalories(
            @CurrentUser Long userId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        // Default to last 30 days if not specified
        LocalDate endDate = to != null ? to : LocalDate.now();
        LocalDate startDate = from != null ? from : endDate.minusDays(30);
        return statsService.getDailyCalories(userId, startDate, endDate);
    }

    @GetMapping("/streak")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get consecutive active days streak for authenticated user")
    public StreakDto getStreak(@CurrentUser Long userId) {
        return statsService.getStreak(userId);
    }
}

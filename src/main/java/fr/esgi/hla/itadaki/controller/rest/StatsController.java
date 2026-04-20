package fr.esgi.hla.itadaki.controller.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle nutritional statistics endpoints.
 *       - GET /api/stats/overview          → overall stats for authenticated user
 *       - GET /api/stats/daily             → daily calorie breakdown
 *       - GET /api/stats/daily?from=&to=   → calorie breakdown for a date range
 *       Inject StatsService.
 *       Return StatsOverviewDto / List<DailyCaloriesDto>.
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {
    // TODO: Inject StatsService
    // TODO: Implement GET overview endpoint
    // TODO: Implement GET daily calories endpoint
}

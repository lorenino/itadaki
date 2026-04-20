package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle nutritional statistics endpoints.
 *       - GET api/stats/overview          → overall stats for authenticated user
 *       - GET api/stats/daily             → daily calorie breakdown
 *       - GET api/stats/daily?from=&to=   → calorie breakdown for a date range
 *       Inject StatsService.
 *       Return StatsOverviewDto / List<DailyCaloriesDto>.
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/stats")
@Tag(name = "Statistics", description = "Nutritional statistics endpoints")
public class StatsController {

    private StatsService statsService;

    // TODO: @Operation(summary = "Get overall nutrition stats for authenticated user") GET /overview → StatsOverviewDto
    // TODO: @Operation(summary = "Get daily calorie breakdown") GET /daily → List<DailyCaloriesDto>
    // TODO: @Operation(summary = "Get calorie breakdown for date range") GET /daily?from=&to= → List<DailyCaloriesDto>
}

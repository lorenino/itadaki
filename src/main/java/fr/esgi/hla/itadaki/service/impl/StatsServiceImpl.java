package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TODO: Implements StatsService.
 *       - getOverview: aggregate meals and analyses for userId, compute totals and averages
 *       - getDailyCalories: group analyses by date within range, return daily breakdown
 *
 *       Inject: MealAnalysisRepository, MealRepository, StatsMapper
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    // TODO: Inject MealAnalysisRepository
    // TODO: Inject MealRepository
    // TODO: Inject StatsMapper

    // TODO: Override getOverview(Long userId) → StatsOverviewDto
    // TODO: Override getDailyCalories(Long userId, LocalDate from, LocalDate to) → List<DailyCaloriesDto>
}

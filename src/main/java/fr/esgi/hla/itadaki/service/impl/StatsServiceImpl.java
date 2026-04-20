package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.dto.stats.DailyCaloriesDto;
import fr.esgi.hla.itadaki.dto.stats.StatsOverviewDto;
import fr.esgi.hla.itadaki.repository.MealAnalysisRepository;
import fr.esgi.hla.itadaki.repository.MealRepository;
import fr.esgi.hla.itadaki.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of StatsService.
 * Aggregates nutritional statistics from user meal data.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final MealAnalysisRepository mealAnalysisRepository;
    private final MealRepository mealRepository;

    @Override
    public StatsOverviewDto getOverview(Long userId) {
        // Get all meals for user
        List<Meal> meals = mealRepository.findAllByUserId(userId, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        if (meals.isEmpty()) {
            return new StatsOverviewDto(0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null);
        }

        // Calculate totals and averages
        double totalCalories = 0.0;
        int mealCount = meals.size();

        for (Meal meal : meals) {
            if (meal.getAnalysis() != null && meal.getAnalysis().getEstimatedTotalCalories() != null) {
                totalCalories += meal.getAnalysis().getEstimatedTotalCalories();
            }
        }

        // Calculate average daily calories (approximation: total / days since first meal)
        LocalDateTime firstMealTime = meals.stream()
                .map(Meal::getUploadedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        long daysSinceFirst = ChronoUnit.DAYS.between(firstMealTime.toLocalDate(), LocalDate.now()) + 1;
        double avgDailyCalories = totalCalories / Math.max(daysSinceFirst, 1);

        return new StatsOverviewDto(
                mealCount,
                totalCalories,
                avgDailyCalories,
                0.0, // TODO: Calculate averages from analysis items
                0.0,
                0.0,
                firstMealTime.toLocalDate().toString(),
                LocalDate.now().toString()
        );
    }

    @Override
    public List<DailyCaloriesDto> getDailyCalories(Long userId, LocalDate from, LocalDate to) {
        // Get meals in date range
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        List<Meal> meals = mealRepository.findAllByUserIdAndUploadedAtBetween(userId, fromDateTime, toDateTime);

        // Group by date and sum calories
        Map<LocalDate, Double> dailyTotals = new HashMap<>();
        Map<LocalDate, Integer> dailyCounts = new HashMap<>();

        for (Meal meal : meals) {
            LocalDate date = meal.getUploadedAt().toLocalDate();
            double calories = 0.0;
            if (meal.getAnalysis() != null && meal.getAnalysis().getEstimatedTotalCalories() != null) {
                calories = meal.getAnalysis().getEstimatedTotalCalories();
            }

            dailyTotals.put(date, dailyTotals.getOrDefault(date, 0.0) + calories);
            dailyCounts.put(date, dailyCounts.getOrDefault(date, 0) + 1);
        }

        // Convert to DTOs
        return dailyTotals.entrySet().stream()
                .map(entry -> new DailyCaloriesDto(
                        entry.getKey().toString(),
                        entry.getValue(),
                        dailyCounts.getOrDefault(entry.getKey(), 0)
                ))
                .sorted((a, b) -> b.date().compareTo(a.date()))
                .collect(Collectors.toList());
    }
}

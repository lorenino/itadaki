package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.stats.DailyCaloriesDto;
import fr.esgi.hla.itadaki.dto.stats.StatsOverviewDto;
import fr.esgi.hla.itadaki.dto.stats.StreakDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for nutritional statistics operations.
 * - getOverview(Long userId)                                     → StatsOverviewDto
 * - getDailyCalories(Long userId, LocalDate from, LocalDate to)  → List<DailyCaloriesDto>
 * - getStreak(Long userId)                                       → StreakDto
 */
public interface StatsService {

    StatsOverviewDto getOverview(Long userId);

    List<DailyCaloriesDto> getDailyCalories(Long userId, LocalDate from, LocalDate to);

    StreakDto getStreak(Long userId);
}

package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.stats.DailyCaloriesDto;
import fr.esgi.hla.itadaki.dto.stats.StatsOverviewDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for nutritional statistics operations.
 * - getOverview(Long userId)                                     → StatsOverviewDto
 * - getDailyCalories(Long userId, LocalDate from, LocalDate to)  → List<DailyCaloriesDto>
 */
public interface StatsService {

    StatsOverviewDto getOverview(Long userId);

    List<DailyCaloriesDto> getDailyCalories(Long userId, LocalDate from, LocalDate to);
}

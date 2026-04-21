package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.stats.DailyCaloriesDto;
import fr.esgi.hla.itadaki.dto.stats.DinnerSuggestionDto;
import fr.esgi.hla.itadaki.dto.stats.StatsOverviewDto;
import fr.esgi.hla.itadaki.dto.stats.StreakDto;
import fr.esgi.hla.itadaki.dto.stats.WeeklySummaryDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for nutritional statistics operations.
 */
public interface StatsService {

    StatsOverviewDto getOverview(Long userId);

    List<DailyCaloriesDto> getDailyCalories(Long userId, LocalDate from, LocalDate to);

    StreakDto getStreak(Long userId);

    /**
     * Bilan narratif des 7 derniers jours genere par Ollama.
     */
    WeeklySummaryDto getWeeklySummary(Long userId);

    /**
     * Suggestion du prochain repas par Ollama, basee sur l'historique
     * recent + l'heure courante (petit-dej/dej/goûter/diner).
     */
    DinnerSuggestionDto getMealSuggestion(Long userId);
}

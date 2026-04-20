package fr.esgi.hla.itadaki.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.business.MealAnalysis;
import fr.esgi.hla.itadaki.dto.stats.DailyCaloriesDto;
import fr.esgi.hla.itadaki.dto.stats.StatsOverviewDto;
import fr.esgi.hla.itadaki.repository.MealAnalysisRepository;
import fr.esgi.hla.itadaki.repository.MealRepository;
import fr.esgi.hla.itadaki.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final MealAnalysisRepository mealAnalysisRepository;
    private final MealRepository mealRepository;
    private final ObjectMapper objectMapper;

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
        double totalProtein = 0.0;
        double totalCarbs = 0.0;
        double totalFat = 0.0;
        int mealCount = meals.size();
        int mealsWithMacros = 0; // denominateur pour moyenne des macros

        for (Meal meal : meals) {
            MealAnalysis analysis = meal.getAnalysis();
            if (analysis == null) continue;

            if (analysis.getEstimatedTotalCalories() != null) {
                totalCalories += analysis.getEstimatedTotalCalories();
            }

            // Parse detectedItemsJson pour sommer les macros par ingredient
            // (format persiste depuis commit dd7da30 :
            // {"ingredients":[{"nom":"...","caloriesApprox":X,"proteines":Y,"glucides":Z,"lipides":W},...]})
            double[] mealMacros = extractMealMacros(analysis.getDetectedItemsJson());
            if (mealMacros != null) {
                totalProtein += mealMacros[0];
                totalCarbs   += mealMacros[1];
                totalFat     += mealMacros[2];
                mealsWithMacros++;
            }
        }

        // Calculate average daily calories (approximation: total / days since first meal)
        LocalDateTime firstMealTime = meals.stream()
                .map(Meal::getUploadedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        long daysSinceFirst = ChronoUnit.DAYS.between(firstMealTime.toLocalDate(), LocalDate.now()) + 1;
        double avgDailyCalories = totalCalories / Math.max(daysSinceFirst, 1);

        // Moyennes macros PAR REPAS (pas par jour : plus pertinent pour un "profil nutritionnel")
        double avgProtein = mealsWithMacros > 0 ? totalProtein / mealsWithMacros : 0.0;
        double avgCarbs   = mealsWithMacros > 0 ? totalCarbs   / mealsWithMacros : 0.0;
        double avgFat     = mealsWithMacros > 0 ? totalFat     / mealsWithMacros : 0.0;

        return new StatsOverviewDto(
                mealCount,
                totalCalories,
                avgDailyCalories,
                avgProtein,
                avgCarbs,
                avgFat,
                firstMealTime.toLocalDate().toString(),
                LocalDate.now().toString()
        );
    }

    /**
     * Parse detectedItemsJson et somme les macros (proteines/glucides/lipides)
     * de tous les ingredients du repas. Retourne null si aucune macro trouvee
     * (pour que le denominateur de moyenne ne soit pas fausse par des repas
     * sans breakdown nutritionnel).
     */
    private double[] extractMealMacros(String detectedItemsJson) {
        if (detectedItemsJson == null || detectedItemsJson.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(detectedItemsJson);
            JsonNode items = root.get("ingredients");
            if (items == null || !items.isArray()) return null;

            double prot = 0, carbs = 0, fat = 0;
            boolean found = false;
            for (JsonNode item : items) {
                if (!item.isObject()) continue;
                if (item.has("proteines") && !item.get("proteines").isNull()) {
                    prot += item.get("proteines").asDouble(0);
                    found = true;
                }
                if (item.has("glucides") && !item.get("glucides").isNull()) {
                    carbs += item.get("glucides").asDouble(0);
                    found = true;
                }
                if (item.has("lipides") && !item.get("lipides").isNull()) {
                    fat += item.get("lipides").asDouble(0);
                    found = true;
                }
            }
            return found ? new double[]{prot, carbs, fat} : null;
        } catch (Exception ex) {
            log.debug("Could not parse detectedItemsJson for stats macros: {}", ex.getMessage());
            return null;
        }
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

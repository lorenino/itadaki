package fr.esgi.hla.itadaki.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.business.MealAnalysis;
import fr.esgi.hla.itadaki.business.enums.MealType;
import fr.esgi.hla.itadaki.dto.stats.DailyCaloriesDto;
import fr.esgi.hla.itadaki.dto.stats.DinnerSuggestionDto;
import fr.esgi.hla.itadaki.dto.stats.StatsOverviewDto;
import fr.esgi.hla.itadaki.dto.stats.StreakDto;
import fr.esgi.hla.itadaki.dto.stats.WeeklySummaryDto;
import fr.esgi.hla.itadaki.repository.MealAnalysisRepository;
import fr.esgi.hla.itadaki.repository.MealRepository;
import fr.esgi.hla.itadaki.service.OllamaService;
import fr.esgi.hla.itadaki.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Aggregates nutritional statistics from user meal data. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StatsServiceImpl implements StatsService {

    private static final String FIELD_PROTEINES        = "proteines";
    private static final String FIELD_GLUCIDES         = "glucides";
    private static final String FIELD_LIPIDES          = "lipides";
    private static final String FIELD_ESTIMATED_CALS   = "estimatedCalories";
    private static final String FIELD_INGREDIENTS      = "ingredients";

    private final MealAnalysisRepository mealAnalysisRepository;
    private final MealRepository mealRepository;
    private final ObjectMapper objectMapper;
    private final OllamaService ollamaService;

    @Override
    public StatsOverviewDto getOverview(Long userId) {
        List<Meal> meals = mealRepository.findAllByUserId(userId, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        if (meals.isEmpty()) {
            return new StatsOverviewDto(0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null);
        }

        double totalCalories = 0.0;
        double totalProtein = 0.0;
        double totalCarbs = 0.0;
        double totalFat = 0.0;
        int mealCount = meals.size();
        int mealsWithMacros = 0;

        for (Meal meal : meals) {
            MealAnalysis analysis = meal.getAnalysis();
            if (analysis == null) continue;

            if (analysis.getEstimatedTotalCalories() != null) {
                totalCalories += analysis.getEstimatedTotalCalories();
            }

            double[] mealMacros = extractMealMacros(analysis.getDetectedItemsJson());
            if (mealMacros.length > 0) {
                totalProtein += mealMacros[0];
                totalCarbs   += mealMacros[1];
                totalFat     += mealMacros[2];
                mealsWithMacros++;
            }
        }

        LocalDateTime firstMealTime = meals.stream()
                .map(Meal::getUploadedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        long daysSinceFirst = ChronoUnit.DAYS.between(firstMealTime.toLocalDate(), LocalDate.now()) + 1;
        double avgDailyCalories = totalCalories / Math.max(daysSinceFirst, 1);

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

    /** Returns [prot, carbs, fat] summed from ingredients JSON, or empty array if unavailable. */
    private double[] extractMealMacros(String detectedItemsJson) {
        if (detectedItemsJson == null || detectedItemsJson.isBlank()) return new double[0];
        try {
            JsonNode root = objectMapper.readTree(detectedItemsJson);
            JsonNode items = root.get(FIELD_INGREDIENTS);
            if (items == null || !items.isArray()) return new double[0];
            return sumIngredientMacros(items);
        } catch (Exception ex) {
            log.debug("Could not parse detectedItemsJson for stats macros: {}", ex.getMessage());
            return new double[0];
        }
    }

    private double[] sumIngredientMacros(JsonNode items) {
        double prot = 0;
        double carbs = 0;
        double fat = 0;
        boolean found = false;
        for (JsonNode item : items) {
            if (!item.isObject()) continue;
            if (item.has(FIELD_PROTEINES) && !item.get(FIELD_PROTEINES).isNull()) {
                prot += item.get(FIELD_PROTEINES).asDouble(0);
                found = true;
            }
            if (item.has(FIELD_GLUCIDES) && !item.get(FIELD_GLUCIDES).isNull()) {
                carbs += item.get(FIELD_GLUCIDES).asDouble(0);
                found = true;
            }
            if (item.has(FIELD_LIPIDES) && !item.get(FIELD_LIPIDES).isNull()) {
                fat += item.get(FIELD_LIPIDES).asDouble(0);
                found = true;
            }
        }
        return found ? new double[]{prot, carbs, fat} : new double[0];
    }

    @Override
    public List<DailyCaloriesDto> getDailyCalories(Long userId, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        List<Meal> meals = mealRepository.findAllByUserIdAndUploadedAtBetween(userId, fromDateTime, toDateTime);

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

        return dailyTotals.entrySet().stream()
                .map(entry -> new DailyCaloriesDto(
                        entry.getKey().toString(),
                        entry.getValue(),
                        dailyCounts.getOrDefault(entry.getKey(), 0)
                ))
                .sorted((a, b) -> b.date().compareTo(a.date()))
                .toList();
    }

    /**
     * Calcule la série de jours consécutifs actifs.
     * current : série depuis aujourd'hui (ou hier si aujourd'hui inactif).
     * longest : plus longue série jamais réalisée.
     */
    @Override
    public StreakDto getStreak(Long userId) {
        List<Meal> allMeals = mealRepository.findAllByUserId(
                userId, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent();

        if (allMeals.isEmpty()) {
            return new StreakDto(0, 0);
        }

        Set<LocalDate> activeDays = new HashSet<>();
        for (Meal meal : allMeals) {
            activeDays.add(meal.getUploadedAt().toLocalDate());
        }

        LocalDate today = LocalDate.now();
        LocalDate cursor = activeDays.contains(today) ? today : today.minusDays(1);
        int current = 0;
        while (activeDays.contains(cursor)) {
            current++;
            cursor = cursor.minusDays(1);
        }

        List<LocalDate> sortedDays = activeDays.stream().sorted().toList();
        int longest = 0;
        int run = 0;
        LocalDate prev = null;
        for (LocalDate d : sortedDays) {
            if (prev == null || d.equals(prev.plusDays(1))) {
                run++;
            } else {
                run = 1;
            }
            if (run > longest) longest = run;
            prev = d;
        }

        return new StreakDto(current, longest);
    }

    // ─── Fonctionnalites IA : bilan hebdo + suggestion de repas ─────────────

    private static final String SUMMARY_SYSTEM_PROMPT = """
            Tu es un coach nutrition chaleureux et positif qui tutoie l'utilisateur.
            Ecris un bilan de 3 a 4 phrases en francais, sans markdown, sans listes.
            Mentionne 1 force concrete et 1 axe d'amelioration. Reste factuel, pas de morale.
            Termine par une encouragement court. N'utilise jamais d'emoji.
            """;

    private static final String SUGGESTION_SYSTEM_PROMPT = """
            Tu es un coach nutrition qui suggere un plat concret et realiste pour le prochain repas.
            Retourne UNIQUEMENT du JSON valide selon ce schema, sans markdown ni texte autour :
            {"dishName": string, "reason": string, "estimatedCalories": integer}
            - dishName : nom d'un plat reel en francais (ex. "Poke bowl saumon-avocat", "Omelette aux champignons").
            - reason : 1 phrase courte expliquant pourquoi ce choix vu les stats recentes (tutoiement).
            - estimatedCalories : estimation realiste en kcal (entier positif entre 200 et 900).
            """;

    @Override
    public WeeklySummaryDto getWeeklySummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(6);
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = today.plusDays(1).atStartOfDay();

        List<Meal> meals = mealRepository.findAllByUserIdAndUploadedAtBetween(userId, fromDt, toDt);
        WeeklyStats ws = aggregateWeeklyMeals(meals);

        String bestDay = ws.byDay().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().toString())
                .orElse(null);
        Double bestDayKcal = ws.byDay().values().stream().max(Double::compareTo).orElse(null);
        String topDish = ws.dishCounts().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        String summary = buildSummaryText(meals, ws, from, today, bestDay, bestDayKcal, topDish);

        return new WeeklySummaryDto(
                summary,
                from.toString(),
                today.toString(),
                meals.size(),
                ws.totalKcal(),
                bestDay,
                bestDayKcal
        );
    }

    private WeeklyStats aggregateWeeklyMeals(List<Meal> meals) {
        Map<LocalDate, Double> byDay = new HashMap<>();
        double totalKcal = 0;
        double totalProt = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        int mealsWithMacros = 0;
        Map<String, Integer> dishCounts = new HashMap<>();

        for (Meal m : meals) {
            LocalDate d = m.getUploadedAt().toLocalDate();
            MealAnalysis a = m.getAnalysis();
            double kcal = a != null && a.getEstimatedTotalCalories() != null ? a.getEstimatedTotalCalories() : 0;
            byDay.merge(d, kcal, Double::sum);
            totalKcal += kcal;
            if (a != null && a.getDetectedDishName() != null) {
                dishCounts.merge(a.getDetectedDishName(), 1, Integer::sum);
            }
            if (a != null) {
                double[] macros = extractMealMacros(a.getDetectedItemsJson());
                if (macros.length > 0) {
                    totalProt += macros[0];
                    totalCarbs += macros[1];
                    totalFat += macros[2];
                    mealsWithMacros++;
                }
            }
        }
        return new WeeklyStats(byDay, totalKcal, totalProt, totalCarbs, totalFat, mealsWithMacros, dishCounts);
    }

    private String buildSummaryText(List<Meal> meals, WeeklyStats ws,
                                    LocalDate from, LocalDate today,
                                    String bestDay, Double bestDayKcal, String topDish) {
        if (meals.size() < 2) {
            return "Pas encore assez de repas cette semaine pour un bilan detaille. Continue a scanner tes plats, et ton coach IA prendra le relais des la semaine prochaine !";
        }
        double avgKcal = ws.totalKcal() / Math.max(ws.byDay().size(), 1);
        double avgProt = ws.mealsWithMacros() > 0 ? ws.totalProt() / ws.mealsWithMacros() : 0;
        double avgCarbs = ws.mealsWithMacros() > 0 ? ws.totalCarbs() / ws.mealsWithMacros() : 0;
        double avgFat = ws.mealsWithMacros() > 0 ? ws.totalFat() / ws.mealsWithMacros() : 0;

        String userPrompt = """
                Periode : du %s au %s (%d jours actifs).
                Repas scannes : %d. Calories totales : %.0f kcal. Moyenne journaliere : %.0f kcal.
                Macros moyennes par repas : proteines %.0f g, glucides %.0f g, lipides %.0f g.
                Meilleur jour en apport : %s (%.0f kcal).
                Plat le plus frequent : %s.
                """.formatted(
                from, today, ws.byDay().size(), meals.size(), ws.totalKcal(), avgKcal,
                avgProt, avgCarbs, avgFat,
                bestDay != null ? bestDay : "non defini", bestDayKcal != null ? bestDayKcal : 0,
                topDish != null ? topDish : "aucun"
        );

        try {
            return ollamaService.chatText(SUMMARY_SYSTEM_PROMPT, userPrompt, false).trim();
        } catch (Exception ex) {
            log.warn("Weekly summary LLM call failed, using fallback: {}", ex.getMessage());
            return "Tu as scanne %d repas sur les 7 derniers jours pour un total de %.0f kcal. Continue comme ca !"
                    .formatted(meals.size(), ws.totalKcal());
        }
    }

    private record WeeklyStats(Map<LocalDate, Double> byDay, double totalKcal,
                                double totalProt, double totalCarbs, double totalFat,
                                int mealsWithMacros, Map<String, Integer> dishCounts) {}

    @Override
    public DinnerSuggestionDto getMealSuggestion(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        MealType nextType = MealType.detectFromTime(now);

        LocalDate today = LocalDate.now();
        LocalDateTime fromDt = today.minusDays(6).atStartOfDay();
        LocalDateTime toDt = today.plusDays(1).atStartOfDay();
        List<Meal> meals = mealRepository.findAllByUserIdAndUploadedAtBetween(userId, fromDt, toDt);

        RecentStats rs = aggregateRecentMeals(meals);

        double avgProt = rs.mealsWithMacros() > 0 ? rs.totalProt() / rs.mealsWithMacros() : 0;
        double avgCarbs = rs.mealsWithMacros() > 0 ? rs.totalCarbs() / rs.mealsWithMacros() : 0;
        double avgFat = rs.mealsWithMacros() > 0 ? rs.totalFat() / rs.mealsWithMacros() : 0;

        String typeFr = switch (nextType) {
            case BREAKFAST -> "petit-dejeuner";
            case LUNCH -> "dejeuner";
            case SNACK -> "gouter";
            case DINNER -> "diner";
        };

        String userPrompt = """
                Prochain repas : %s (il est %02d:%02d).
                Stats 7 derniers jours : %d repas, %.0f kcal total.
                Moyennes macros par repas : proteines %.0f g, glucides %.0f g, lipides %.0f g.
                Plats deja manges recemment (evite de redonder) : %s.
                Suggere 1 plat equilibre adapte a cette heure et a ces stats, en evitant de reproposer un plat deja vu.
                """.formatted(
                typeFr, now.getHour(), now.getMinute(),
                meals.size(), rs.totalKcal(),
                avgProt, avgCarbs, avgFat,
                rs.recentDishes().isEmpty() ? "aucun" : String.join(", ", rs.recentDishes())
        );

        return parseSuggestion(userPrompt, nextType);
    }

    private RecentStats aggregateRecentMeals(List<Meal> meals) {
        double totalProt = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        double totalKcal = 0;
        int mealsWithMacros = 0;
        Set<String> recentDishes = new HashSet<>();

        for (Meal m : meals) {
            MealAnalysis a = m.getAnalysis();
            if (a == null) continue;
            if (a.getEstimatedTotalCalories() != null) totalKcal += a.getEstimatedTotalCalories();
            if (a.getDetectedDishName() != null) recentDishes.add(a.getDetectedDishName());
            double[] macros = extractMealMacros(a.getDetectedItemsJson());
            if (macros.length > 0) {
                totalProt += macros[0];
                totalCarbs += macros[1];
                totalFat += macros[2];
                mealsWithMacros++;
            }
        }
        return new RecentStats(totalProt, totalCarbs, totalFat, totalKcal, mealsWithMacros, recentDishes);
    }

    private DinnerSuggestionDto parseSuggestion(String userPrompt, MealType nextType) {
        try {
            String raw = ollamaService.chatText(SUGGESTION_SYSTEM_PROMPT, userPrompt, true);
            JsonNode node = objectMapper.readTree(raw);
            String dish = node.has("dishName") ? node.get("dishName").asText("Suggestion indisponible") : "Suggestion indisponible";
            String reason = node.has("reason") ? node.get("reason").asText("") : "";
            Integer kcal = node.has(FIELD_ESTIMATED_CALS) && node.get(FIELD_ESTIMATED_CALS).canConvertToInt()
                    ? node.get(FIELD_ESTIMATED_CALS).asInt() : null;
            return new DinnerSuggestionDto(dish, reason, kcal, nextType.name());
        } catch (Exception ex) {
            log.warn("Meal suggestion LLM call failed: {}", ex.getMessage());
            String fallback = switch (nextType) {
                case BREAKFAST -> "Porridge avoine-fruits rouges";
                case LUNCH -> "Poke bowl saumon-avocat";
                case SNACK -> "Fromage blanc-noix";
                case DINNER -> "Curry de lentilles-legumes";
            };
            return new DinnerSuggestionDto(
                    fallback,
                    "Ollama indisponible, suggestion par defaut equilibree pour ce creneau.",
                    null,
                    nextType.name()
            );
        }
    }

    private record RecentStats(double totalProt, double totalCarbs, double totalFat,
                               double totalKcal, int mealsWithMacros, Set<String> recentDishes) {}
}

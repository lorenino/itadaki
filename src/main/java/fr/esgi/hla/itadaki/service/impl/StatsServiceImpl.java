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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final OllamaService ollamaService;

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

    /**
     * Calcule la série de jours consécutifs actifs de l'utilisateur.
     * Un jour est "actif" s'il contient au moins 1 meal.
     * current : série depuis aujourd'hui en remontant (ou depuis hier si aujourd'hui inactif).
     * longest : plus longue série jamais réalisée.
     */
    @Override
    public StreakDto getStreak(Long userId) {
        // Récupère tous les repas de l'utilisateur (sans limite)
        List<Meal> allMeals = mealRepository.findAllByUserId(
                userId, org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent();

        if (allMeals.isEmpty()) {
            return new StreakDto(0, 0);
        }

        // Ensemble des jours actifs
        Set<LocalDate> activeDays = new HashSet<>();
        for (Meal meal : allMeals) {
            activeDays.add(meal.getUploadedAt().toLocalDate());
        }

        LocalDate today = LocalDate.now();

        // Calcul du current streak :
        // On tolère que aujourd'hui soit inactif (le jour n'est pas terminé).
        // On part de "today" si actif, sinon de "yesterday".
        LocalDate cursor = activeDays.contains(today) ? today : today.minusDays(1);
        int current = 0;
        while (activeDays.contains(cursor)) {
            current++;
            cursor = cursor.minusDays(1);
        }

        // Calcul du longest streak : on trie les jours actifs et on cherche la plus longue suite
        List<LocalDate> sortedDays = activeDays.stream()
                .sorted()
                .collect(Collectors.toList());

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

        // Breakdown par jour
        Map<LocalDate, Double> byDay = new HashMap<>();
        double totalKcal = 0;
        double totalProt = 0, totalCarbs = 0, totalFat = 0;
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
                if (macros != null) {
                    totalProt += macros[0];
                    totalCarbs += macros[1];
                    totalFat += macros[2];
                    mealsWithMacros++;
                }
            }
        }

        String bestDay = byDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().toString())
                .orElse(null);
        Double bestDayKcal = byDay.values().stream().max(Double::compareTo).orElse(null);
        String topDish = dishCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        String summary;
        if (meals.size() < 2) {
            summary = "Pas encore assez de repas cette semaine pour un bilan detaille. Continue a scanner tes plats, et ton coach IA prendra le relais des la semaine prochaine !";
        } else {
            double avgKcal = totalKcal / Math.max(byDay.size(), 1);
            double avgProt = mealsWithMacros > 0 ? totalProt / mealsWithMacros : 0;
            double avgCarbs = mealsWithMacros > 0 ? totalCarbs / mealsWithMacros : 0;
            double avgFat = mealsWithMacros > 0 ? totalFat / mealsWithMacros : 0;

            String userPrompt = """
                    Periode : du %s au %s (%d jours actifs).
                    Repas scannes : %d. Calories totales : %.0f kcal. Moyenne journaliere : %.0f kcal.
                    Macros moyennes par repas : proteines %.0f g, glucides %.0f g, lipides %.0f g.
                    Meilleur jour en apport : %s (%.0f kcal).
                    Plat le plus frequent : %s.
                    """.formatted(
                    from, today, byDay.size(), meals.size(), totalKcal, avgKcal,
                    avgProt, avgCarbs, avgFat,
                    bestDay != null ? bestDay : "non defini", bestDayKcal != null ? bestDayKcal : 0,
                    topDish != null ? topDish : "aucun"
            );

            try {
                summary = ollamaService.chatText(SUMMARY_SYSTEM_PROMPT, userPrompt, false).trim();
            } catch (Exception ex) {
                log.warn("Weekly summary LLM call failed, using fallback: {}", ex.getMessage());
                summary = "Tu as scanne %d repas sur les 7 derniers jours pour un total de %.0f kcal. Continue comme ca !"
                        .formatted(meals.size(), totalKcal);
            }
        }

        return new WeeklySummaryDto(
                summary,
                from.toString(),
                today.toString(),
                meals.size(),
                totalKcal,
                bestDay,
                bestDayKcal
        );
    }

    @Override
    public DinnerSuggestionDto getMealSuggestion(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        MealType nextType = MealType.detectFromTime(now);

        // Contexte : stats 7 derniers jours (reutilise getWeeklySummary en interne)
        LocalDate today = LocalDate.now();
        LocalDateTime fromDt = today.minusDays(6).atStartOfDay();
        LocalDateTime toDt = today.plusDays(1).atStartOfDay();
        List<Meal> meals = mealRepository.findAllByUserIdAndUploadedAtBetween(userId, fromDt, toDt);

        double totalProt = 0, totalCarbs = 0, totalFat = 0, totalKcal = 0;
        int mealsWithMacros = 0;
        Set<String> recentDishes = new HashSet<>();
        for (Meal m : meals) {
            MealAnalysis a = m.getAnalysis();
            if (a == null) continue;
            if (a.getEstimatedTotalCalories() != null) totalKcal += a.getEstimatedTotalCalories();
            if (a.getDetectedDishName() != null) recentDishes.add(a.getDetectedDishName());
            double[] macros = extractMealMacros(a.getDetectedItemsJson());
            if (macros != null) {
                totalProt += macros[0];
                totalCarbs += macros[1];
                totalFat += macros[2];
                mealsWithMacros++;
            }
        }

        double avgProt = mealsWithMacros > 0 ? totalProt / mealsWithMacros : 0;
        double avgCarbs = mealsWithMacros > 0 ? totalCarbs / mealsWithMacros : 0;
        double avgFat = mealsWithMacros > 0 ? totalFat / mealsWithMacros : 0;

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
                meals.size(), totalKcal,
                avgProt, avgCarbs, avgFat,
                recentDishes.isEmpty() ? "aucun" : String.join(", ", recentDishes)
        );

        try {
            String raw = ollamaService.chatText(SUGGESTION_SYSTEM_PROMPT, userPrompt, true);
            JsonNode node = objectMapper.readTree(raw);
            String dish = node.has("dishName") ? node.get("dishName").asText("Suggestion indisponible") : "Suggestion indisponible";
            String reason = node.has("reason") ? node.get("reason").asText("") : "";
            Integer kcal = node.has("estimatedCalories") && node.get("estimatedCalories").canConvertToInt()
                    ? node.get("estimatedCalories").asInt() : null;
            return new DinnerSuggestionDto(dish, reason, kcal, nextType.name());
        } catch (Exception ex) {
            log.warn("Meal suggestion LLM call failed: {}", ex.getMessage());
            // Fallback simple mais honnete
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
}

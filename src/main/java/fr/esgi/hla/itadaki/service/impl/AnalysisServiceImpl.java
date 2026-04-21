package fr.esgi.hla.itadaki.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.esgi.hla.itadaki.business.Meal;
import fr.esgi.hla.itadaki.business.MealAnalysis;
import fr.esgi.hla.itadaki.business.enums.MealStatus;
import fr.esgi.hla.itadaki.dto.analysis.MealAnalysisResponseDto;
import fr.esgi.hla.itadaki.dto.analysis.ReanalyzeRequestDto;
import fr.esgi.hla.itadaki.dto.meal.DetectedFoodItemDto;
import fr.esgi.hla.itadaki.exception.MealAnalysisException;
import fr.esgi.hla.itadaki.exception.ResourceNotFoundException;
import fr.esgi.hla.itadaki.mapper.MealAnalysisMapper;
import fr.esgi.hla.itadaki.repository.MealAnalysisRepository;
import fr.esgi.hla.itadaki.repository.MealRepository;
import fr.esgi.hla.itadaki.service.AnalysisService;
import fr.esgi.hla.itadaki.service.MealPhotoService;
import fr.esgi.hla.itadaki.service.OllamaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Orchestrates meal image analysis via Ollama with JSON response parsing. */
@Service
@RequiredArgsConstructor
@Transactional
public class AnalysisServiceImpl implements AnalysisService {

    private final MealRepository mealRepository;
    private final MealAnalysisRepository mealAnalysisRepository;
    private final OllamaService ollamaService;
    private final MealPhotoService mealPhotoService;
    private final MealAnalysisMapper mealAnalysisMapper;
    private final ObjectMapper objectMapper;

    @Override
    public MealAnalysisResponseDto analyzeMeal(Long mealId) {
        Meal meal = findMeal(mealId);
        updateMealStatus(meal, MealStatus.ANALYSING);
        try {
            String rawResponse = callOllama(mealId, null);
            JsonNode json = objectMapper.readTree(rawResponse);

            MealAnalysis analysis = new MealAnalysis();
            analysis.setMeal(meal);
            populateAnalysis(analysis, json, rawResponse);
            analysis = mealAnalysisRepository.save(analysis);

            updateMealStatus(meal, MealStatus.ANALYSED);
            return toResponseDto(analysis, rawResponse);
        } catch (Exception ex) {
            updateMealStatus(meal, MealStatus.FAILED);
            throw new MealAnalysisException("Analysis failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public MealAnalysisResponseDto reanalyzeMeal(Long mealId, ReanalyzeRequestDto request) {
        // UPSERT pattern: reuse existing analysis to avoid TransientPropertyValueException
        // when meal.analysis references a deleted entity.
        Meal meal = findMeal(mealId);
        updateMealStatus(meal, MealStatus.ANALYSING);
        try {
            String rawResponse = callOllama(mealId, request.hint());
            JsonNode json = objectMapper.readTree(rawResponse);

            MealAnalysis analysis = findOrCreateAnalysis(mealId, meal);
            populateAnalysis(analysis, json, rawResponse);
            // Force analyzedAt so polling detects the update (@CreationTimestamp won't change on update).
            analysis.setAnalyzedAt(LocalDateTime.now());
            analysis = mealAnalysisRepository.save(analysis);

            updateMealStatus(meal, MealStatus.ANALYSED);
            return toResponseDto(analysis, rawResponse);
        } catch (Exception ex) {
            updateMealStatus(meal, MealStatus.FAILED);
            throw new MealAnalysisException("Re-analysis failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public MealAnalysisResponseDto getAnalysis(Long mealId) {
        MealAnalysis analysis = mealAnalysisRepository.findByMealId(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found for meal id: " + mealId));
        return toResponseDto(analysis, analysis.getDetectedItemsJson());
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Meal findMeal(Long mealId) {
        return mealRepository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + mealId));
    }

    private void updateMealStatus(Meal meal, MealStatus status) {
        meal.setStatus(status);
        mealRepository.save(meal);
    }

    private String callOllama(Long mealId, String hint) {
        String imagePath = mealPhotoService.getStoredPath(mealId);
        String prompt = ollamaService.buildMealAnalysisPrompt(hint);
        return ollamaService.analyzeImage(imagePath, prompt);
    }

    private MealAnalysis findOrCreateAnalysis(Long mealId, Meal meal) {
        return mealAnalysisRepository.findByMealId(mealId).orElseGet(() -> {
            MealAnalysis a = new MealAnalysis();
            a.setMeal(meal);
            return a;
        });
    }

    private void populateAnalysis(MealAnalysis analysis, JsonNode json, String rawResponse) {
        analysis.setDetectedDishName(extractString(json, "nomPlat"));
        analysis.setDetectedItemsJson(rawResponse);
        analysis.setEstimatedTotalCalories(extractCalories(json));
        analysis.setConfidenceScore(parseConfidence(extractString(json, "confiance")));
        analysis.setRawModelResponse(rawResponse);
    }

    /** Builds response DTO via MapStruct then injects detectedItems parsed from raw JSON. */
    private MealAnalysisResponseDto toResponseDto(MealAnalysis analysis, String itemsJson) {
        MealAnalysisResponseDto dto = mealAnalysisMapper.toDto(analysis);
        return new MealAnalysisResponseDto(
                dto.id(), dto.mealId(), dto.detectedDishName(),
                parseDetectedItems(itemsJson),
                dto.estimatedTotalCalories(), dto.confidenceScore(),
                dto.rawModelResponse(), dto.analyzedAt()
        );
    }

    private String extractString(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    private Double extractCalories(JsonNode node) {
        try {
            if (node.has("caloriesMax") && !node.get("caloriesMax").isNull()) {
                return node.get("caloriesMax").asDouble();
            }
        } catch (Exception ex) {
            // fall through
        }
        return null;
    }

    private Double parseConfidence(String confidence) {
        if (confidence == null) return 0.5;
        return switch (confidence.toLowerCase()) {
            case "haute"   -> 0.9;
            case "moyenne" -> 0.5;
            case "basse"   -> 0.2;
            default        -> 0.5;
        };
    }

    private List<DetectedFoodItemDto> parseDetectedItems(String jsonString) {
        List<DetectedFoodItemDto> items = new ArrayList<>();
        try {
            JsonNode node = objectMapper.readTree(jsonString);
            if (node.has("ingredients") && node.get("ingredients").isArray()) {
                for (JsonNode ingredient : node.get("ingredients")) {
                    if (ingredient.isTextual()) {
                        items.add(ingredientFromString(ingredient.asText()));
                    } else if (ingredient.isObject()) {
                        items.add(ingredientFromObject(ingredient));
                    }
                }
            }
        } catch (Exception ex) {
            // Return empty list on parse failure
        }
        return items;
    }

    private DetectedFoodItemDto ingredientFromString(String name) {
        return new DetectedFoodItemDto(name, 1.0, "portion", null, null, null, null);
    }

    private DetectedFoodItemDto ingredientFromObject(JsonNode n) {
        return new DetectedFoodItemDto(
                n.has("nom") ? n.get("nom").asText() : "inconnu",
                1.0,
                "portion",
                safeDouble(n, "caloriesApprox"),
                safeDouble(n, "proteines"),
                safeDouble(n, "glucides"),
                safeDouble(n, "lipides")
        );
    }

    private Double safeDouble(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asDouble() : null;
    }
}

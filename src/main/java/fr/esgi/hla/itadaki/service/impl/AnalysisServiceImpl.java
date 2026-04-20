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

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of AnalysisService.
 * Orchestrates meal image analysis via Ollama with JSON response parsing.
 */
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
        // Fetch meal and verify it exists
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + mealId));

        // Update status to ANALYSING
        meal.setStatus(MealStatus.ANALYSING);
        mealRepository.save(meal);

        try {
            // Get stored path from MealPhotoService
            String imagePath = mealPhotoService.getStoredPath(mealId);

            // Build prompt and call Ollama
            String prompt = ollamaService.buildMealAnalysisPrompt(null);
            String rawResponse = ollamaService.analyzeImage(imagePath, prompt);

            // Parse JSON response
            JsonNode jsonNode = objectMapper.readTree(rawResponse);

            // Create MealAnalysis entity
            MealAnalysis analysis = new MealAnalysis();
            analysis.setMeal(meal);
            analysis.setDetectedDishName(extractString(jsonNode, "nomPlat"));
            analysis.setDetectedItemsJson(rawResponse);
            analysis.setEstimatedTotalCalories(extractCalories(jsonNode));
            analysis.setConfidenceScore(parseConfidence(extractString(jsonNode, "confiance")));
            analysis.setRawModelResponse(rawResponse);

            analysis = mealAnalysisRepository.save(analysis);

            // Update meal status to ANALYSED
            meal.setStatus(MealStatus.ANALYSED);
            mealRepository.save(meal);

            return toResponseDto(analysis, rawResponse);
        } catch (Exception ex) {
            meal.setStatus(MealStatus.FAILED);
            mealRepository.save(meal);
            throw new MealAnalysisException("Analysis failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public MealAnalysisResponseDto reanalyzeMeal(Long mealId, ReanalyzeRequestDto request) {
        // UPSERT pattern : on reutilise l'analyse existante plutot que delete+insert.
        // Le delete+insert cassait la relation OneToOne Meal<->MealAnalysis
        // (TransientPropertyValueException quand meal.analysis reference l'entite supprimee).
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + mealId));

        meal.setStatus(MealStatus.ANALYSING);
        mealRepository.save(meal);

        try {
            String imagePath = mealPhotoService.getStoredPath(mealId);
            String prompt = ollamaService.buildMealAnalysisPrompt(request.hint());
            String rawResponse = ollamaService.analyzeImage(imagePath, prompt);

            JsonNode jsonNode = objectMapper.readTree(rawResponse);

            // Trouve l'analyse existante ou en cree une nouvelle
            MealAnalysis analysis = mealAnalysisRepository.findByMealId(mealId)
                    .orElseGet(() -> {
                        MealAnalysis a = new MealAnalysis();
                        a.setMeal(meal);
                        return a;
                    });
            analysis.setDetectedDishName(extractString(jsonNode, "nomPlat"));
            analysis.setDetectedItemsJson(rawResponse);
            analysis.setEstimatedTotalCalories(extractCalories(jsonNode));
            analysis.setConfidenceScore(parseConfidence(extractString(jsonNode, "confiance")));
            analysis.setRawModelResponse(rawResponse);
            // Force analyzedAt = now pour que le polling front detecte la mise a jour
            // (CreationTimestamp ne change pas sur un update)
            analysis.setAnalyzedAt(java.time.LocalDateTime.now());

            analysis = mealAnalysisRepository.save(analysis);

            meal.setStatus(MealStatus.ANALYSED);
            mealRepository.save(meal);

            return toResponseDto(analysis, rawResponse);
        } catch (Exception ex) {
            meal.setStatus(MealStatus.FAILED);
            mealRepository.save(meal);
            throw new MealAnalysisException("Re-analysis failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public MealAnalysisResponseDto getAnalysis(Long mealId) {
        MealAnalysis analysis = mealAnalysisRepository.findByMealId(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found for meal id: " + mealId));
        return toResponseDto(analysis, analysis.getDetectedItemsJson());
    }

    /**
     * Hydrate une MealAnalysisResponseDto via le mapper MapStruct puis injecte
     * detectedItems parse depuis le JSON brut (MapStruct ignore ce champ).
     */
    private MealAnalysisResponseDto toResponseDto(MealAnalysis analysis, String itemsJson) {
        MealAnalysisResponseDto dto = mealAnalysisMapper.toDto(analysis);
        List<DetectedFoodItemDto> detectedItems = parseDetectedItems(itemsJson);
        return new MealAnalysisResponseDto(
                dto.id(),
                dto.mealId(),
                dto.detectedDishName(),
                detectedItems,
                dto.estimatedTotalCalories(),
                dto.confidenceScore(),
                dto.rawModelResponse(),
                dto.analyzedAt()
        );
    }

    private String extractString(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return null;
    }

    private Double extractCalories(JsonNode node) {
        try {
            if (node.has("caloriesMax") && !node.get("caloriesMax").isNull()) {
                return node.get("caloriesMax").asDouble();
            }
        } catch (Exception ex) {
            // Ignore parsing errors
        }
        return null;
    }

    private Double parseConfidence(String confidence) {
        if (confidence == null) {
            return 0.5;
        }
        return switch (confidence.toLowerCase()) {
            case "haute" -> 0.9;
            case "moyenne" -> 0.5;
            case "basse" -> 0.2;
            default -> 0.5;
        };
    }

    private List<DetectedFoodItemDto> parseDetectedItems(String jsonString) {
        List<DetectedFoodItemDto> items = new ArrayList<>();
        try {
            JsonNode node = objectMapper.readTree(jsonString);
            if (node.has("ingredients") && node.get("ingredients").isArray()) {
                for (JsonNode ingredient : node.get("ingredients")) {
                    if (ingredient.isTextual()) {
                        // Rétro-compatibilité : ancien format string[]
                        DetectedFoodItemDto item = new DetectedFoodItemDto(
                                ingredient.asText(),
                                1.0,
                                "portion",
                                null,
                                null,
                                null,
                                null
                        );
                        items.add(item);
                    } else if (ingredient.isObject()) {
                        // Nouveau format : {nom, caloriesApprox, proteines, glucides, lipides}
                        String nom = ingredient.has("nom") ? ingredient.get("nom").asText() : "inconnu";
                        Double calories = ingredient.has("caloriesApprox") && !ingredient.get("caloriesApprox").isNull()
                                ? ingredient.get("caloriesApprox").asDouble() : null;
                        Double proteines = ingredient.has("proteines") && !ingredient.get("proteines").isNull()
                                ? ingredient.get("proteines").asDouble() : null;
                        Double glucides = ingredient.has("glucides") && !ingredient.get("glucides").isNull()
                                ? ingredient.get("glucides").asDouble() : null;
                        Double lipides = ingredient.has("lipides") && !ingredient.get("lipides").isNull()
                                ? ingredient.get("lipides").asDouble() : null;
                        DetectedFoodItemDto item = new DetectedFoodItemDto(
                                nom,
                                1.0,
                                "portion",
                                calories,
                                proteines,
                                glucides,
                                lipides
                        );
                        items.add(item);
                    }
                }
            }
        } catch (Exception ex) {
            // Si le parsing échoue, retourne la liste vide
        }
        return items;
    }
}

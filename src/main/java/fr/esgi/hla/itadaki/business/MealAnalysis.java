package fr.esgi.hla.itadaki.business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/** Stores the AI analysis result for a meal; owning side of the Meal↔MealAnalysis 1:1 (FK: meal_id). */
@Entity
@Table(name = "meal_analyses")
@Data
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class MealAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Dish name as detected by the AI model (e.g. "Sushi platter").
     */
    @Column
    private String detectedDishName;

    /**
     * JSON array of detected food items with per-item nutritional breakdown.
     * Stored as TEXT to accommodate variable-length AI output.
     * Example: [{"name":"salmon","calories":120,"protein":15.0,...},...]
     */
    @Column(columnDefinition = "TEXT")
    private String detectedItemsJson;

    /**
     * Total estimated calories for the entire meal as returned by the AI model.
     */
    @Column
    private Double estimatedTotalCalories;

    /**
     * Confidence score between 0.0 and 1.0 indicating how certain the model is.
     */
    @Column
    private Double confidenceScore;

    /**
     * Full raw JSON response from the Ollama model, preserved for debugging and re-parsing.
     */
    @Column(columnDefinition = "TEXT")
    private String rawModelResponse;

    @CreationTimestamp
    private LocalDateTime analyzedAt;

    /**
     * Owning side of MealAnalysis→Meal (1:1). FK column: meal_id.
     */
    @OneToOne
    @JoinColumn(name = "meal_id", nullable = false)
    @NotNull(message = "MealAnalysis must be linked to a meal")
    private Meal meal;
}

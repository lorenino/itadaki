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

/**
 * JPA entity storing user-submitted corrections applied to a meal analysis.
 * Optional (0..1) — only created when the user disagrees with the AI analysis.
 *
 * Owning side of the Meal↔MealCorrection 0..1 relationship: holds the FK column meal_id.
 */
@Entity
@Table(name = "meal_corrections")
@Data
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class MealCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Dish name as corrected by the user (may differ from AI-detected name).
     */
    @Column
    private String correctedDishName;

    /**
     * JSON array of corrected food items with per-item nutritional values as provided by the user.
     * Stored as TEXT to match the detectedItemsJson format of MealAnalysis.
     */
    @Column(columnDefinition = "TEXT")
    private String correctedItemsJson;

    /**
     * Total calorie count after user correction.
     */
    @Column
    private Double correctedTotalCalories;

    /**
     * Optional free-text comment from the user explaining the correction.
     */
    @Column
    private String userComment;

    @CreationTimestamp
    private LocalDateTime correctedAt;

    /**
     * Owning side of MealCorrection→Meal (0..1). FK column: meal_id.
     */
    @OneToOne
    @JoinColumn(name = "meal_id", nullable = false)
    @NotNull(message = "MealCorrection must be linked to a meal")
    private Meal meal;
}

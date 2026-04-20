package fr.esgi.hla.itadaki.business;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity storing the AI analysis result for a meal.
 *
 * TODO: Add fields: detectedItems (JSON or @ElementCollection), totalCalories,
 *       totalProtein, totalCarbs, totalFat, rawAiResponse, analyzedAt
 * TODO: Add @OneToOne relation back to Meal
 * TODO: Consider storing detected food items as a JSON blob or separate table
 * TODO: Add Lombok @Builder, @NoArgsConstructor, @AllArgsConstructor as needed
 */
@Entity
@Table(name = "meal_analyses")
@Getter
@Setter
public class MealAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Add Double totalCalories
    // TODO: Add Double totalProtein
    // TODO: Add Double totalCarbs
    // TODO: Add Double totalFat
    // TODO: Add String rawAiResponse (Ollama raw JSON output)
    // TODO: Add LocalDateTime analyzedAt
    // TODO: Add Meal meal (OneToOne, mappedBy or joinColumn)
}

package fr.esgi.hla.itadaki.business;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * JPA entity storing the AI analysis result for a meal.
 *
 * TODO: Add Double totalCalories
 * TODO: Add Double totalProtein
 * TODO: Add Double totalCarbs
 * TODO: Add Double totalFat
 * TODO: Add String rawAiResponse  (raw Ollama JSON output — store as TEXT column)
 * TODO: Add @CreationTimestamp LocalDateTime analyzedAt
 * TODO: Add @OneToOne @JoinColumn(name = "meal_id") @NotNull Meal meal  (owning side)
 * TODO: Consider @Column(columnDefinition = "TEXT") for rawAiResponse
 * TODO: Consider storing detected food items as a separate @ElementCollection or JSON column
 */
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

    // TODO: Double totalCalories
    // TODO: Double totalProtein
    // TODO: Double totalCarbs
    // TODO: Double totalFat
    // TODO: @Column(columnDefinition = "TEXT") String rawAiResponse
    // TODO: @CreationTimestamp LocalDateTime analyzedAt
    // TODO: @OneToOne @JoinColumn(name = "meal_id") @NotNull Meal meal
}

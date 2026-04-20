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
 * JPA entity storing user-submitted corrections applied to a meal analysis.
 *
 * TODO: Add Double correctedCalories
 * TODO: Add Double correctedProtein
 * TODO: Add Double correctedCarbs
 * TODO: Add Double correctedFat
 * TODO: Add String note  (optional user comment)
 * TODO: Add @CreationTimestamp LocalDateTime correctedAt
 * TODO: Add @OneToOne @JoinColumn(name = "meal_id") @NotNull Meal meal  (owning side)
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

    // TODO: Double correctedCalories
    // TODO: Double correctedProtein
    // TODO: Double correctedCarbs
    // TODO: Double correctedFat
    // TODO: String note
    // TODO: @CreationTimestamp LocalDateTime correctedAt
    // TODO: @OneToOne @JoinColumn(name = "meal_id") @NotNull Meal meal
}

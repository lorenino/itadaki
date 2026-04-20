package fr.esgi.hla.itadaki.business;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity storing user corrections applied to a meal analysis.
 *
 * TODO: Add fields: correctedItems (JSON or @ElementCollection), correctedCalories,
 *       correctedProtein, correctedCarbs, correctedFat, correctedAt, note
 * TODO: Add @OneToOne relation back to Meal
 * TODO: Add Lombok @Builder, @NoArgsConstructor, @AllArgsConstructor as needed
 */
@Entity
@Table(name = "meal_corrections")
@Getter
@Setter
public class MealCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Add Double correctedCalories
    // TODO: Add Double correctedProtein
    // TODO: Add Double correctedCarbs
    // TODO: Add Double correctedFat
    // TODO: Add String note (optional user comment)
    // TODO: Add LocalDateTime correctedAt
    // TODO: Add Meal meal (OneToOne, mappedBy or joinColumn)
}

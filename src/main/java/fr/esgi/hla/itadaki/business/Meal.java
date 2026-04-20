package fr.esgi.hla.itadaki.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.esgi.hla.itadaki.business.enums.MealStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing one meal consumption event submitted by a user.
 * Central aggregate: owns MealPhoto (1:1), MealAnalysis (1:1), MealCorrection (0..1).
 *
 * Owning sides (FK in this table): user
 * Non-owning sides (mappedBy): photo, analysis, correction
 */
@Entity
@Table(name = "meals")
@Data
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Meal status is required")
    private MealStatus status;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Owning side of Meal→User. FK column: user_id.
     */
    @ManyToOne
    @NotNull(message = "Meal must belong to a user")
    private User user;

    /**
     * Non-owning side of Meal→MealPhoto (1:1).
     * MealPhoto holds the FK (meal_id). Cascades removal so the photo row is deleted with the meal.
     */
    @OneToOne(mappedBy = "meal", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    @JsonIgnore
    private MealPhoto photo;

    /**
     * Non-owning side of Meal→MealAnalysis (1:1).
     * MealAnalysis holds the FK (meal_id). Cascades removal.
     */
    @OneToOne(mappedBy = "meal", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    @JsonIgnore
    private MealAnalysis analysis;

    /**
     * Non-owning side of Meal→MealCorrection (0..1).
     * MealCorrection holds the FK (meal_id). Cascades removal.
     */
    @OneToOne(mappedBy = "meal", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    @JsonIgnore
    private MealCorrection correction;
}

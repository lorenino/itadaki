package fr.esgi.hla.itadaki.business;

import fr.esgi.hla.itadaki.business.enums.MealStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * JPA entity representing a meal submitted by a user for analysis.
 * A Meal is the central aggregate: it owns a MealPhoto, a MealAnalysis and a MealCorrection.
 *
 * TODO: Add @CreationTimestamp LocalDateTime uploadedAt
 * TODO: Add @UpdateTimestamp LocalDateTime updatedAt
 * TODO: Add @ManyToOne @NotNull User user  (owning side — FK column)
 * TODO: Add @OneToOne(mappedBy = "meal", cascade = CascadeType.REMOVE) @ToString.Exclude MealPhoto photo
 * TODO: Add @OneToOne(mappedBy = "meal", cascade = CascadeType.REMOVE) @ToString.Exclude MealAnalysis analysis
 * TODO: Add @OneToOne(mappedBy = "meal", cascade = CascadeType.REMOVE) @ToString.Exclude MealCorrection correction
 * TODO: Add @Column constraints
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
    private MealStatus status;

    // TODO: @CreationTimestamp LocalDateTime uploadedAt
    // TODO: @UpdateTimestamp LocalDateTime updatedAt
    // TODO: @ManyToOne @NotNull User user
    // TODO: @OneToOne(mappedBy = "meal", cascade = CascadeType.REMOVE) @ToString.Exclude MealPhoto photo
    // TODO: @OneToOne(mappedBy = "meal", cascade = CascadeType.REMOVE) @ToString.Exclude MealAnalysis analysis
    // TODO: @OneToOne(mappedBy = "meal", cascade = CascadeType.REMOVE) @ToString.Exclude MealCorrection correction
}

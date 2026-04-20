package fr.esgi.hla.itadaki.business;

import fr.esgi.hla.itadaki.business.enums.MealStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity representing a meal submitted by a user for analysis.
 *
 * TODO: Add remaining fields: imagePath, uploadedAt, analyzedAt
 * TODO: Add @ManyToOne relation to User (owner)
 * TODO: Add @OneToOne relation to MealAnalysis
 * TODO: Add @OneToOne relation to MealCorrection
 * TODO: Add @Column constraints
 * TODO: Add Lombok @Builder, @NoArgsConstructor, @AllArgsConstructor as needed
 */
@Entity
@Table(name = "meals")
@Getter
@Setter
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: Add String imagePath (path or URL to stored image)
    // TODO: Add LocalDateTime uploadedAt
    // TODO: Add LocalDateTime analyzedAt

    @Enumerated(EnumType.STRING)
    private MealStatus status;

    // TODO: Add User user (ManyToOne)
    // TODO: Add MealAnalysis analysis (OneToOne)
    // TODO: Add MealCorrection correction (OneToOne)
}

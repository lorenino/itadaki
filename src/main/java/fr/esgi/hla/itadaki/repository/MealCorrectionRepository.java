package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.MealCorrection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for MealCorrection entity. Spring Data detects this automatically — no @Repository needed.
 * TODO: Optional<MealCorrection> findByMealId(Long mealId);
 */
public interface MealCorrectionRepository extends JpaRepository<MealCorrection, Long> {
    // TODO: Add query methods listed above
}

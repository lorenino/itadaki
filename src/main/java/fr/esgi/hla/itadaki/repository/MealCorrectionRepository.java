package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.MealCorrection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for MealCorrection entity. Spring Data detects this automatically — no @Repository needed.
 */
public interface MealCorrectionRepository extends JpaRepository<MealCorrection, Long> {

    Optional<MealCorrection> findByMealId(Long mealId);

    void deleteByMealId(Long mealId);
}

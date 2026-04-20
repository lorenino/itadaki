package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.MealCorrection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO: Repository for MealCorrection entity.
 *       Custom queries to add:
 *       - findByMealId(Long mealId) → Optional<MealCorrection>
 */
@Repository
public interface MealCorrectionRepository extends JpaRepository<MealCorrection, Long> {
    // TODO: Add custom query methods listed above
}

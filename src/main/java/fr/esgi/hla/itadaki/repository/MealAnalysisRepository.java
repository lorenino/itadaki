package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.MealAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for MealAnalysis entity. Spring Data detects this automatically — no @Repository needed.
 * TODO: Optional<MealAnalysis> findByMealId(Long mealId);
 * TODO: Aggregation queries for statistics (sum/avg of calories per user per day).
 */
public interface MealAnalysisRepository extends JpaRepository<MealAnalysis, Long> {
    // TODO: Add query methods listed above
}

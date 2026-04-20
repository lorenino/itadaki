package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.MealAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO: Repository for MealAnalysis entity.
 *       Custom queries to add:
 *       - findByMealId(Long mealId) → Optional<MealAnalysis>
 *       - Aggregation queries for statistics (sum/avg of calories per user per day)
 */
@Repository
public interface MealAnalysisRepository extends JpaRepository<MealAnalysis, Long> {
    // TODO: Add custom query methods listed above
}

package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.MealAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Repository for MealAnalysis entity. Spring Data detects this automatically — no @Repository needed.
 */
public interface MealAnalysisRepository extends JpaRepository<MealAnalysis, Long> {

    Optional<MealAnalysis> findByMealId(Long mealId);

    void deleteByMealId(Long mealId);

    @Query("SELECT AVG(a.estimatedTotalCalories) FROM MealAnalysis a WHERE a.estimatedTotalCalories IS NOT NULL")
    Double averageEstimatedCalories();
}

package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.MealPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for MealPhoto entity. Spring Data detects this automatically — no @Repository needed.
 */
public interface MealPhotoRepository extends JpaRepository<MealPhoto, Long> {

    Optional<MealPhoto> findByMealId(Long mealId);

    void deleteByMealId(Long mealId);
}

package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.Meal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Meal entity. Spring Data detects this automatically — no @Repository needed.
 * TODO: Page<Meal> findAllByUserId(Long userId, Pageable pageable);
 * TODO: List<Meal> findAllByUserIdAndUploadedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);
 * TODO: long countByUserId(Long userId);
 */
public interface MealRepository extends JpaRepository<Meal, Long> {
    // TODO: Add query methods listed above
}

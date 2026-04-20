package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO: Repository for Meal entity.
 *       Custom queries to add:
 *       - findAllByUserId(Long userId, Pageable pageable) → Page<Meal>
 *       - findAllByUserIdAndUploadedAtBetween(...) → for date-range history
 *       - countByUserId(Long userId) → long
 */
@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    // TODO: Add custom query methods listed above
}

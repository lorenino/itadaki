package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.Meal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Meal entity. Spring Data detects this automatically — no @Repository needed.
 */
public interface MealRepository extends JpaRepository<Meal, Long> {

    Page<Meal> findAllByUserId(Long userId, Pageable pageable);

    List<Meal> findAllByUserIdAndUploadedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);

    long countByUserId(Long userId);

    @Query("""
            SELECT m
            FROM Meal m
            WHERE m.user.id = :userId
              AND CAST(m.uploadedAt AS date) = :date
            ORDER BY m.uploadedAt DESC
            """)
    List<Meal> findByUserIdAndDate(@Param("userId") Long userId,
                                   @Param("date") LocalDate date);
}

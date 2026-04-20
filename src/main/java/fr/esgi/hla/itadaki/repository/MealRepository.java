package fr.esgi.hla.itadaki.repository;

import fr.esgi.hla.itadaki.business.Meal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * Force la mise à jour de uploadedAt (utilisé par DataSeeder pour horodater
     * les repas démo dans le passé, contournant @CreationTimestamp).
     */
    @Modifying
    @Query("UPDATE Meal m SET m.uploadedAt = :uploadedAt WHERE m.id = :id")
    void updateUploadedAt(@Param("id") Long id, @Param("uploadedAt") LocalDateTime uploadedAt);
}

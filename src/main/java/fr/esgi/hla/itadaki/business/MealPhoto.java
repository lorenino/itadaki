package fr.esgi.hla.itadaki.business;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/**
 * JPA entity representing the uploaded image file associated with a meal.
 * Separated from Meal to keep the photo metadata (path, MIME type, size)
 * in its own concern, allowing independent storage management.
 *
 * TODO: Add @Column(nullable = false) String originalFilename
 * TODO: Add @Column(nullable = false) String storedPath  (relative path on disk / storage key)
 * TODO: Add @Column(nullable = false) String mimeType    (e.g. "image/jpeg")
 * TODO: Add @Column(nullable = false) Long fileSizeBytes
 * TODO: Add @OneToOne @JoinColumn(name = "meal_id") @NotNull Meal meal  (owning side — FK)
 * TODO: Add @Column constraints (nullable, length)
 */
@Entity
@Table(name = "meal_photos")
@Data
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
public class MealPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: @Column(nullable = false) String originalFilename
    // TODO: @Column(nullable = false) String storedPath
    // TODO: @Column(nullable = false) String mimeType
    // TODO: @Column(nullable = false) Long fileSizeBytes

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    // TODO: @OneToOne @JoinColumn(name = "meal_id") @NotNull Meal meal
}

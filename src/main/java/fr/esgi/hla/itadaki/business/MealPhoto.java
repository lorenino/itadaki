package fr.esgi.hla.itadaki.business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/** Stores file metadata (path, MIME type, size) for a meal image — no binary data; owning side of Meal↔MealPhoto (FK: meal_id). */
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

    /**
     * Original filename as provided by the client (e.g. "lunch.jpg").
     */
    @Column(nullable = false)
    @NotBlank(message = "Original file name is required")
    private String originalFileName;

    /**
     * Server-generated filename (UUID-based) used on disk (e.g. "a3f2...jpg").
     */
    @Column(nullable = false)
    @NotBlank(message = "File name is required")
    private String fileName;

    /**
     * Relative path from the upload root directory (e.g. "uploads/a3f2...jpg").
     * Used by OllamaService and FileStorageService to locate the file on disk.
     */
    @Column(nullable = false)
    @NotBlank(message = "Storage path is required")
    private String storagePath;

    /**
     * MIME type of the uploaded file (e.g. "image/jpeg", "image/png").
     */
    @Column(nullable = false)
    @NotBlank(message = "Content type is required")
    private String contentType;

    /**
     * File size in bytes. Must be positive.
     */
    @Column(nullable = false)
    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    private Long size;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    /**
     * Owning side of MealPhoto→Meal (1:1). FK column: meal_id.
     */
    @OneToOne
    @JoinColumn(name = "meal_id", nullable = false)
    @NotNull(message = "MealPhoto must be linked to a meal")
    private Meal meal;
}

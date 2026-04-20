package fr.esgi.hla.itadaki.business.enums;

/**
 * Represents the lifecycle status of a meal analysis.
 * Stored as a STRING column on the Meal entity (@Enumerated(EnumType.STRING)).
 *
 * PENDING   → meal uploaded, analysis not yet started
 * ANALYSING → AI model is currently processing the image
 * ANALYSED  → analysis completed successfully
 * CORRECTED → user submitted a manual correction over the analysis
 * FAILED    → analysis failed (AI error or invalid image)
 */
public enum MealStatus {
    PENDING,
    ANALYSING,
    ANALYSED,
    CORRECTED,
    FAILED
}

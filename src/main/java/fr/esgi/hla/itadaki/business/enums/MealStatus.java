package fr.esgi.hla.itadaki.business.enums;

/**
 * TODO: Represents the lifecycle status of a meal analysis.
 *       - PENDING    : meal image uploaded, analysis not yet started
 *       - ANALYSING  : AI model is currently processing the image
 *       - ANALYSED   : analysis completed successfully
 *       - CORRECTED  : user submitted a manual correction
 *       - FAILED     : analysis failed (AI error or invalid image)
 *       Will be stored as a String column on the Meal entity.
 */
public enum MealStatus {
    PENDING,
    ANALYSING,
    ANALYSED,
    CORRECTED,
    FAILED
}

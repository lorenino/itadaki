package fr.esgi.hla.itadaki.business.enums;

/** Lifecycle of a meal analysis: PENDING → ANALYSING → ANALYSED | CORRECTED | FAILED. */
public enum MealStatus {
    PENDING,
    ANALYSING,
    ANALYSED,
    CORRECTED,
    FAILED
}

package fr.esgi.hla.itadaki.business.enums;

import java.time.LocalDateTime;

/**
 * Categorie de repas utilisee pour classer l'historique (filtres, stats).
 * Detectee automatiquement depuis l'heure d'upload, modifiable par l'utilisateur.
 *
 * BREAKFAST  → 5h-11h
 * LUNCH      → 11h-15h
 * SNACK      → 15h-18h
 * DINNER     → 18h-5h (nuit comprise)
 */
public enum MealType {
    BREAKFAST,
    LUNCH,
    SNACK,
    DINNER;

    /**
     * Retourne le type de repas probable selon l'heure donnee.
     * Ne modifie pas l'etat : utile pour @PrePersist et suggestions front.
     */
    public static MealType detectFromTime(LocalDateTime when) {
        if (when == null) return LUNCH;
        int h = when.getHour();
        if (h >= 5 && h < 11) return BREAKFAST;
        if (h >= 11 && h < 15) return LUNCH;
        if (h >= 15 && h < 18) return SNACK;
        return DINNER;
    }
}

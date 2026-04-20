package fr.esgi.hla.itadaki.dto.stats;

import java.io.Serializable;

/**
 * DTO pour la série de jours consécutifs d'un utilisateur.
 * current : nombre de jours consécutifs actifs jusqu'à aujourd'hui (ou hier si aujourd'hui pas actif).
 * longest : plus longue série consécutive jamais réalisée.
 */
public record StreakDto(int current, int longest) implements Serializable {
}

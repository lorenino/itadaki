package fr.esgi.hla.itadaki.ai;

import java.util.List;

/**
 * Sortie du LLM multimodal Ollama (décodage JSON via BeanOutputConverter Spring AI).
 *
 * Schéma imposé dans le prompt système (cf. Model-IA/PROMPTS.md) :
 * {
 *   "nomPlat":       "string (FR)",
 *   "ingredients":   ["string (FR)", ...],
 *   "portion":       "petit" | "moyen" | "grand",
 *   "caloriesMin":   int (positive, < caloriesMax),
 *   "caloriesMax":   int (positive, > caloriesMin),
 *   "confiance":     "haute" | "moyenne" | "basse"
 * }
 */
public record AnalyseResult(
        String nomPlat,
        List<String> ingredients,
        String portion,
        int caloriesMin,
        int caloriesMax,
        String confiance
) {

    /** Fallback si le LLM ne peut rien identifier ou si parsing JSON échoue 2 fois. */
    public static AnalyseResult inconnu() {
        return new AnalyseResult("inconnu", List.of(), "moyen", 0, 0, "basse");
    }

    public boolean estInconnu() {
        return "inconnu".equalsIgnoreCase(nomPlat);
    }

    /** Validation basique (appeler après désérialisation). */
    public boolean estValide() {
        return nomPlat != null && !nomPlat.isBlank()
                && ingredients != null
                && portion != null && List.of("petit", "moyen", "grand").contains(portion)
                && confiance != null && List.of("haute", "moyenne", "basse").contains(confiance)
                && caloriesMin >= 0 && caloriesMax >= caloriesMin;
    }
}

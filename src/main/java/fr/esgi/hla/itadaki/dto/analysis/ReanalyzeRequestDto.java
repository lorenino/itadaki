package fr.esgi.hla.itadaki.dto.analysis;

import java.io.Serializable;

/**
 * DTO for requesting a re-analysis of an already-uploaded meal.
 * TODO: hint is injected into the Ollama prompt to guide re-analysis.
 */
public record ReanalyzeRequestDto(
        String hint
) implements Serializable {
}

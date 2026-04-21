package fr.esgi.hla.itadaki.dto.analysis;

import java.io.Serializable;

/** DTO for requesting a re-analysis of an already-uploaded meal. */
public record ReanalyzeRequestDto(
        String hint
) implements Serializable {
}

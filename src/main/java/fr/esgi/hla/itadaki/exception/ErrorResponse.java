package fr.esgi.hla.itadaki.exception;

import java.io.Serializable;

/** Standardized API error payload returned by GlobalExceptionHandler — no stack traces. */
public record ErrorResponse(
        int status,
        String message,
        String path
) implements Serializable {
}

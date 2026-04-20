package fr.esgi.hla.itadaki.exception;

import java.io.Serializable;

/**
 * Standardized error response payload returned by GlobalExceptionHandler.
 * Used for all API error responses — never leak stack traces.
 */
public record ErrorResponse(
        int status,
        String message,
        String path
) implements Serializable {
}

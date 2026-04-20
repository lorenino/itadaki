package fr.esgi.hla.itadaki.exception;

/**
 * Thrown when an authenticated user tries to act on a resource
 * they do not own (e.g. delete another user's meal).
 * GlobalExceptionHandler maps this to HTTP 403 Forbidden.
 *
 * Distinct from UnauthorizedException (401) which signals
 * absent/invalid authentication.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}

package fr.esgi.hla.itadaki.exception;

/**
 * Thrown when a resource already exists and cannot be created
 * (e.g. email/username already taken on register).
 * GlobalExceptionHandler maps this to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

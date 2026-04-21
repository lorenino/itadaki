package fr.esgi.hla.itadaki.exception;

/** Thrown when a requested resource is not found; mapped to HTTP 404 by GlobalExceptionHandler. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

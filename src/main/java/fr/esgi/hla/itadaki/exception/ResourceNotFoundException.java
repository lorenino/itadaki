package fr.esgi.hla.itadaki.exception;

/**
 * TODO: Thrown when a requested resource (User, Meal, Analysis, etc.) is not found.
 *       GlobalExceptionHandler maps this to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    // TODO: Add resource name and identifier fields for richer error messages
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

package fr.esgi.hla.itadaki.exception;

/**
 * TODO: Thrown when an authenticated user tries to access a resource
 *       they do not own or do not have permission for.
 *       GlobalExceptionHandler maps this to HTTP 401 or 403.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}

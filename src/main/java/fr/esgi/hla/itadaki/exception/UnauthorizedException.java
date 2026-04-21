package fr.esgi.hla.itadaki.exception;

/** Thrown when an authenticated user lacks permission for a resource; mapped to HTTP 401 or 403. */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}

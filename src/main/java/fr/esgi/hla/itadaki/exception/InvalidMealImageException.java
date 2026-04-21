package fr.esgi.hla.itadaki.exception;

/** Thrown when an uploaded file is not a valid meal image (wrong MIME type, too large, corrupt); mapped to HTTP 400. */
public class InvalidMealImageException extends RuntimeException {

    public InvalidMealImageException(String message) {
        super(message);
    }
}

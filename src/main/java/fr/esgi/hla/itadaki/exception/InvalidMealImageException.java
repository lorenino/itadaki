package fr.esgi.hla.itadaki.exception;

/**
 * TODO: Thrown when an uploaded file is not a valid meal image.
 *       Reasons: wrong MIME type, file too large, corrupt image, etc.
 *       GlobalExceptionHandler maps this to HTTP 400.
 */
public class InvalidMealImageException extends RuntimeException {

    public InvalidMealImageException(String message) {
        super(message);
    }
}

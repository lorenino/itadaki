package fr.esgi.hla.itadaki.exception;

/** Thrown when AI meal analysis fails (Ollama unavailable, model error, unparseable response); mapped to HTTP 500. */
public class MealAnalysisException extends RuntimeException {

    public MealAnalysisException(String message) {
        super(message);
    }

    public MealAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}

package fr.esgi.hla.itadaki.exception;

/**
 * TODO: Thrown when the AI analysis of a meal fails.
 *       Reasons: Ollama service unavailable, model error, unparseable AI response.
 *       GlobalExceptionHandler maps this to HTTP 500.
 */
public class MealAnalysisException extends RuntimeException {

    public MealAnalysisException(String message) {
        super(message);
    }

    public MealAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}

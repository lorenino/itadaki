package fr.esgi.hla.itadaki.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * TODO: Centralized exception handler for the REST API.
 *       Catches application-specific and Spring exceptions and maps them
 *       to standardized ErrorResponse payloads with appropriate HTTP status codes.
 *
 *       Exceptions to handle:
 *       - ResourceNotFoundException     → 404 Not Found
 *       - UnauthorizedException         → 401 Unauthorized
 *       - InvalidMealImageException     → 400 Bad Request
 *       - MealAnalysisException         → 500 Internal Server Error
 *       - MethodArgumentNotValidException → 400 with field validation errors
 *       - Generic Exception              → 500 fallback
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // TODO: @ExceptionHandler(ResourceNotFoundException.class) → 404
    // TODO: @ExceptionHandler(UnauthorizedException.class)     → 401
    // TODO: @ExceptionHandler(InvalidMealImageException.class) → 400
    // TODO: @ExceptionHandler(MealAnalysisException.class)     → 500
    // TODO: @ExceptionHandler(MethodArgumentNotValidException.class) → 400 with field errors
    // TODO: @ExceptionHandler(Exception.class)                 → 500 fallback
}

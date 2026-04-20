package fr.esgi.hla.itadaki.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to inject the currently authenticated user ID into controller method parameters.
 * Used with a HandlerMethodArgumentResolver to resolve the user ID from the SecurityContext.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
    // Marker annotation — no attributes needed
}

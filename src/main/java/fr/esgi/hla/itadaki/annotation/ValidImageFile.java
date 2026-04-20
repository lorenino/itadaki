package fr.esgi.hla.itadaki.annotation;

import fr.esgi.hla.itadaki.validator.ImageFileValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bean Validation constraint annotation for validating uploaded image files.
 * Backed by ImageFileValidator which checks MIME type, file size, and extension.
 */
@Documented
@Constraint(validatedBy = ImageFileValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImageFile {

    String message() default "Invalid image file: unsupported type or size";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

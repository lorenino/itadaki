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
 * TODO: Bean Validation constraint annotation for validating uploaded image files.
 *       Will be backed by {@link ImageFileValidator}.
 *       Checks MIME type, file size limits, and allowed extensions (jpg, png, webp, etc.).
 */
@Documented
@Constraint(validatedBy = ImageFileValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImageFile {

    // TODO: Customize default message
    String message() default "Invalid image file";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

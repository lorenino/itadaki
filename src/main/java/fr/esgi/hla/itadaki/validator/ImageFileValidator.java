package fr.esgi.hla.itadaki.validator;

import fr.esgi.hla.itadaki.annotation.ValidImageFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO: ConstraintValidator implementation for @ValidImageFile.
 *       Validation rules to implement:
 *       - File must not be null or empty
 *       - Content type must be one of: image/jpeg, image/png, image/webp
 *       - File size must not exceed configured maximum (e.g., 10MB)
 */
public class ImageFileValidator implements ConstraintValidator<ValidImageFile, MultipartFile> {

    // TODO: @Value or constructor injection for max file size and allowed types

    @Override
    public void initialize(ValidImageFile constraintAnnotation) {
        // TODO: Read configuration from annotation attributes if needed
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // TODO: Implement validation logic
        return false; // placeholder — replace with real logic
    }
}

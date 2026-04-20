package fr.esgi.hla.itadaki.validator;

import fr.esgi.hla.itadaki.annotation.ValidImageFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

/**
 * ConstraintValidator implementation for @ValidImageFile.
 * Validates uploaded image files based on content type and size constraints.
 */
public class ImageFileValidator implements ConstraintValidator<ValidImageFile, MultipartFile> {

    private static final Set<String> ALLOWED_TYPES = new HashSet<>();
    // Aligne sur spring.servlet.multipart.max-file-size (30MB).
    // Photos smartphone modernes (12-48MP) depassent souvent 10MB.
    private static final long MAX_FILE_SIZE = 30L * 1024 * 1024;

    static {
        ALLOWED_TYPES.add("image/jpeg");
        ALLOWED_TYPES.add("image/png");
        ALLOWED_TYPES.add("image/webp");
    }

    @Override
    public void initialize(ValidImageFile constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // Allow null — @NotNull handles null separately
        if (file == null) {
            return true;
        }

        // Check file is not empty
        if (file.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File must not be empty")
                    .addConstraintViolation();
            return false;
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File type not allowed. Allowed types: JPEG, PNG, WebP")
                    .addConstraintViolation();
            return false;
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File size exceeds maximum limit of 30MB")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}

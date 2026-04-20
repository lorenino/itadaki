package fr.esgi.hla.itadaki.validator;

import fr.esgi.hla.itadaki.annotation.ValidImageFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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

        // Check magic bytes (defense contre un .txt renomme .jpg)
        if (!hasValidMagicBytes(file)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "File content is not a valid image (expected PNG, JPEG or WebP signature)")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    /**
     * Verifie les premiers octets pour detecter une image reelle :
     * <ul>
     *   <li>PNG : 89 50 4E 47 0D 0A 1A 0A</li>
     *   <li>JPEG : FF D8 FF</li>
     *   <li>WebP : 52 49 46 46 xx xx xx xx 57 45 42 50 (RIFF....WEBP)</li>
     * </ul>
     */
    private boolean hasValidMagicBytes(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] head = in.readNBytes(12);
            if (head.length < 3) return false;

            // JPEG
            if ((head[0] & 0xFF) == 0xFF
                    && (head[1] & 0xFF) == 0xD8
                    && (head[2] & 0xFF) == 0xFF) {
                return true;
            }
            // PNG
            if (head.length >= 8
                    && (head[0] & 0xFF) == 0x89
                    && head[1] == 'P' && head[2] == 'N' && head[3] == 'G'
                    && (head[4] & 0xFF) == 0x0D
                    && (head[5] & 0xFF) == 0x0A
                    && (head[6] & 0xFF) == 0x1A
                    && (head[7] & 0xFF) == 0x0A) {
                return true;
            }
            // WebP : RIFF....WEBP
            if (head.length >= 12
                    && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                    && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P') {
                return true;
            }
            return false;
        } catch (IOException ex) {
            return false;
        }
    }
}

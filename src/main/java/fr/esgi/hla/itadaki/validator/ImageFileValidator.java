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
 * ConstraintValidator for @ValidImageFile.
 * Checks content type, file size, and magic bytes.
 */
public class ImageFileValidator implements ConstraintValidator<ValidImageFile, MultipartFile> {

    private static final Set<String> ALLOWED_TYPES = new HashSet<>();
    // Aligned with spring.servlet.multipart.max-file-size (30MB).
    private static final long MAX_FILE_SIZE = 30L * 1024 * 1024;

    static {
        ALLOWED_TYPES.add("image/jpeg");
        ALLOWED_TYPES.add("image/png");
        ALLOWED_TYPES.add("image/webp");
    }

    @Override
    public void initialize(ValidImageFile constraintAnnotation) {}

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null) return true; // @NotNull handles null separately

        if (file.isEmpty())
            return fail(context, "File must not be empty");

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase()))
            return fail(context, "File type not allowed. Allowed types: JPEG, PNG, WebP");

        if (file.getSize() > MAX_FILE_SIZE)
            return fail(context, "File size exceeds maximum limit of 30MB");

        if (!hasValidMagicBytes(file))
            return fail(context, "File content is not a valid image (expected PNG, JPEG or WebP signature)");

        return true;
    }

    private boolean fail(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }

    /**
     * Checks the first bytes for a real image signature:
     * PNG (89 50 4E 47…), JPEG (FF D8 FF), WebP (RIFF….WEBP).
     */
    private boolean hasValidMagicBytes(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] head = in.readNBytes(12);
            if (head.length < 3) return false;
            return isJpeg(head) || isPng(head) || isWebP(head);
        } catch (IOException ex) {
            return false;
        }
    }

    private boolean isJpeg(byte[] h) {
        return (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] h) {
        return h.length >= 8
                && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                && (h[4] & 0xFF) == 0x0D && (h[5] & 0xFF) == 0x0A
                && (h[6] & 0xFF) == 0x1A && (h[7] & 0xFF) == 0x0A;
    }

    private boolean isWebP(byte[] h) {
        return h.length >= 12
                && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
    }
}

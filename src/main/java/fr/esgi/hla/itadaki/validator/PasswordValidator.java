package fr.esgi.hla.itadaki.validator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates password strength rules for user registration and authentication.
 * Can be used programmatically or as a backing class for custom validation.
 */
@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/]");

    /**
     * Validates password meets all strength requirements.
     */
    public boolean validate(String password) {
        return password != null && !getViolations(password).isEmpty() == false;
    }

    /**
     * Returns list of violations for a given password.
     * Empty list means password is valid.
     */
    public List<String> getViolations(String password) {
        List<String> violations = new ArrayList<>();

        if (password == null || password.length() < MIN_LENGTH) {
            violations.add("Password must be at least " + MIN_LENGTH + " characters");
        }

        if (!UPPERCASE.matcher(password != null ? password : "").find()) {
            violations.add("Password must contain at least one uppercase letter");
        }

        if (!LOWERCASE.matcher(password != null ? password : "").find()) {
            violations.add("Password must contain at least one lowercase letter");
        }

        if (!DIGIT.matcher(password != null ? password : "").find()) {
            violations.add("Password must contain at least one digit");
        }

        if (!SPECIAL_CHAR.matcher(password != null ? password : "").find()) {
            violations.add("Password must contain at least one special character");
        }

        return violations;
    }
}

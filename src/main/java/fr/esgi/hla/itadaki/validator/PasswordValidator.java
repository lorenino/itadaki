package fr.esgi.hla.itadaki.validator;

import org.springframework.stereotype.Component;

/**
 * TODO: Validates password strength rules for user registration.
 *       Rules to enforce:
 *       - Minimum length (e.g., 8 characters)
 *       - At least one uppercase letter
 *       - At least one digit
 *       - At least one special character
 *       May be used as a programmatic validator (called from AuthService)
 *       or as a backing class for a custom @ValidPassword annotation.
 */
@Component
public class PasswordValidator {

    // TODO: Implement validate(String password) → boolean
    // TODO: Implement getViolations(String password) → List<String> (for detailed error messages)
}

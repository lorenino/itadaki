package fr.esgi.hla.itadaki.validator;

import fr.esgi.hla.itadaki.dto.correction.MealCorrectionRequestDto;
import org.springframework.stereotype.Component;

/**
 * TODO: Validates business rules for meal correction requests.
 *       Rules to enforce:
 *       - correctedItems list must not be null or empty
 *       - Each item must have a non-blank name and positive numeric values
 *       - A meal must be in ANALYSED or CORRECTED status to accept corrections
 *       Called programmatically from CorrectionService before persisting.
 */
@Component
public class MealCorrectionValidator {

    // TODO: Implement validate(MealCorrectionRequestDto request) → void (throws on violation)
}

package fr.esgi.hla.itadaki.initialisation;

import org.springframework.stereotype.Component;

/**
 * TODO: Seeds sample/demo data on startup for development and testing.
 *       Should only run when a specific Spring profile is active (e.g., "dev" or "seed").
 *       Use @Profile("dev") or check an application property flag.
 *       Example data: sample users, meals, analyses.
 */
@Component
public class DataSeeder {

    // TODO: Inject repositories as needed (UserRepository, MealRepository, etc.)

    // TODO: Implement @PostConstruct or ApplicationRunner method:
    //       - guard with profile/property check
    //       - insert sample users
    //       - insert sample meals and analyses
}

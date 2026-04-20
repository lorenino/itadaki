package fr.esgi.hla.itadaki.initialisation;

import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Seeds sample/demo data on startup for development and testing.
 * Only runs when the "dev" Spring profile is active.
 */
@Component
@AllArgsConstructor
@Profile("dev")
public class DataSeeder implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Seed data implementation would go here if needed
        // For now, the AdminInitializer and default entities provide sufficient test data
    }
}

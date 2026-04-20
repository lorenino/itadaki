package fr.esgi.hla.itadaki.initialisation;

import fr.esgi.hla.itadaki.business.User;
import fr.esgi.hla.itadaki.business.enums.UserRole;
import fr.esgi.hla.itadaki.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes the default admin user on first startup if no admin exists.
 * Runs after the application context is fully started.
 */
@Component
@AllArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check if admin user already exists
        if (userRepository.existsByEmail("admin@itadaki.fr")) {
            return;
        }

        // Create default admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@itadaki.fr");
        admin.setPasswordHash(passwordEncoder.encode("Admin1234!"));
        admin.setRole(UserRole.ADMIN);

        userRepository.save(admin);
    }
}

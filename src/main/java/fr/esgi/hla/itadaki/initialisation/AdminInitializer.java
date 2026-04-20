package fr.esgi.hla.itadaki.initialisation;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * TODO: Creates the default admin user on first startup if no admin exists.
 *       Should use UserRepository or AuthService to check existence and create.
 *       Admin credentials should come from application.properties or environment variables.
 *       Do NOT hard-code credentials in production.
 */
@Component
public class AdminInitializer {

    // TODO: Inject UserRepository or UserService
    // TODO: Inject PasswordEncoder

    // TODO: Implement ApplicationRunner or @PostConstruct method:
    //       - check if admin user exists
    //       - if not, create admin with default credentials from config
}

package fr.esgi.hla.itadaki.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Configuration to allow access to the H2 web console during development.
 * The H2 console uses frames, which Spring Security blocks by default.
 * This config permits /h2-console/** paths for development use.
 *
 * Only active in dev profile to prevent console exposure in production.
 */
@Configuration
public class H2ConsoleConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/h2-console/**");
    }
}

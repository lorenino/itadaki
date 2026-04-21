package fr.esgi.hla.itadaki.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.context.annotation.Bean;

/** Permits H2 console access during development by bypassing Spring Security for /h2-console/**. */
@Configuration
public class H2ConsoleConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/h2-console/**");
    }
}

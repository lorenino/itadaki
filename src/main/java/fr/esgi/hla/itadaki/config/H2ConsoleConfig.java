package fr.esgi.hla.itadaki.config;

import org.springframework.context.annotation.Configuration;

/**
 * TODO: Configuration to allow access to the H2 web console during development.
 *       The H2 console uses frames, which Spring Security blocks by default.
 *       - Permit /h2-console/** in SecurityConfig (see SecurityConfig TODO)
 *       - Disable frameOptions for the H2 console path (X-Frame-Options: SAMEORIGIN)
 *       This config class may host H2-specific beans or simply document the
 *       security adjustments required in SecurityConfig for H2 console access.
 *
 *       Note: H2 console should only be enabled in dev/test profiles.
 *       Properties: spring.h2.console.enabled=true (already in application.properties)
 */
@Configuration
public class H2ConsoleConfig {

    // TODO: Document or encapsulate H2 console security adjustments
    // TODO: Consider @Profile("dev") to restrict this config to dev environment only
}

package fr.esgi.hla.itadaki.config;

import org.springframework.context.annotation.Configuration;

/**
 * TODO: Spring Security configuration.
 *       - Define SecurityFilterChain bean:
 *           * Disable CSRF (stateless JWT API)
 *           * Configure public endpoints: /api/auth/**, /h2-console/**, /swagger-ui/**, /v3/api-docs/**
 *           * Require authentication for all other endpoints
 *           * Add JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter
 *           * Set session management to STATELESS
 *       - Define PasswordEncoder bean (BCryptPasswordEncoder)
 *       - Define AuthenticationManager bean
 *       - Define AuthenticationProvider bean (DaoAuthenticationProvider)
 *
 *       Inject: CustomUserDetailsService, JwtAuthenticationFilter, JwtAuthenticationEntryPoint
 */
@Configuration
public class SecurityConfig {

    // TODO: Inject CustomUserDetailsService
    // TODO: Inject JwtAuthenticationFilter
    // TODO: Inject JwtAuthenticationEntryPoint

    // TODO: @Bean SecurityFilterChain securityFilterChain(HttpSecurity http)
    // TODO: @Bean PasswordEncoder passwordEncoder()
    // TODO: @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration config)
    // TODO: @Bean AuthenticationProvider authenticationProvider()
}

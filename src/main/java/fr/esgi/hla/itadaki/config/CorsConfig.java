package fr.esgi.hla.itadaki.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * TODO: CORS configuration for the REST API.
 *       - Allow requests from frontend origin(s) (configurable via application.properties)
 *       - Allow headers: Authorization, Content-Type
 *       - Allow methods: GET, POST, PUT, DELETE, OPTIONS
 *       - Expose header: Authorization
 *       - Configure max age for preflight caching
 *
 *       Implement WebMvcConfigurer.addCorsMappings() or define CorsConfigurationSource bean.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // TODO: @Value("${app.cors.allowed-origins}") private String[] allowedOrigins;

    // TODO: @Override addCorsMappings(CorsRegistry registry)
    //       - registry.addMapping("/api/**").allowedOrigins(...).allowedMethods(...)
}

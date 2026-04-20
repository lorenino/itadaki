package fr.esgi.hla.itadaki.security;

/**
 * Holds shared security constants used across the security layer.
 */
public final class SecurityConstants {
    private SecurityConstants() {
    }

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final long JWT_EXPIRATION_MS = 86_400_000L; // 24h
    public static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/h2-console/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/",
            "/index.html",
            "/css/**",
            "/js/**"
    };
}

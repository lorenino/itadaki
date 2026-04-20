package fr.esgi.hla.itadaki.security;

/**
 * TODO: Holds shared security constants used across the security layer.
 *       Constants to define:
 *       - TOKEN_PREFIX       ("Bearer ")
 *       - HEADER_STRING      ("Authorization")
 *       - PUBLIC_URLS        (array of URL patterns that bypass authentication)
 *       - JWT_EXPIRATION_MS  (default token expiration in milliseconds)
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // utility class — no instantiation
    }

    // TODO: public static final String TOKEN_PREFIX = "Bearer ";
    // TODO: public static final String HEADER_STRING = "Authorization";
    // TODO: public static final String[] PUBLIC_URLS = { "/api/auth/**", "/h2-console/**", ... };
    // TODO: public static final long JWT_EXPIRATION_MS = 86_400_000L; // 24h
}

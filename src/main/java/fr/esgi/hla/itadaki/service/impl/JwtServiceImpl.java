package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TODO: Implements JwtService using a JWT library (e.g., jjwt or spring-security-jwt).
 *       - generateToken: build and sign JWT with username, roles, expiry from config
 *       - extractUsername: parse JWT and return subject claim
 *       - isTokenValid: check signature, expiry, and username match
 *
 *       Secret key and expiry should come from application.properties.
 *       Inject: (none initially — reads config from @Value)
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    // TODO: @Value("${app.jwt.secret}") private String secretKey;
    // TODO: @Value("${app.jwt.expiration-ms}") private long expirationMs;

    // TODO: Override generateToken(UserDetails userDetails) → String
    // TODO: Override extractUsername(String token) → String
    // TODO: Override isTokenValid(String token, UserDetails userDetails) → boolean
}

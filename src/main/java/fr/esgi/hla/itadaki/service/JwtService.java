package fr.esgi.hla.itadaki.service;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Service interface for JWT token operations.
 * - generateToken(UserDetails userDetails) → String
 * - extractUsername(String token)          → String
 * - isTokenValid(String token, UserDetails) → boolean
 * - extractExpiration(String token)         → Date
 */
public interface JwtService {

    String generateToken(UserDetails userDetails);

    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}

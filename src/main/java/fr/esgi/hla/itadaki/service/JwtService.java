package fr.esgi.hla.itadaki.service;

import org.springframework.security.core.userdetails.UserDetails;

/** Generates and validates JWT tokens. */
public interface JwtService {

    String generateToken(UserDetails userDetails);

    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}

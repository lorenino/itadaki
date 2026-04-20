package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of JwtService for token generation and validation.
 *
 * STEP 3 Implementation: Provides basic structure for JWT token handling.
 * Full JWT implementation with signature validation will be completed in STEP 4
 * when security configuration is added.
 *
 * TODO STEP 4: Add JJWT library dependency and implement full JWT encoding/decoding
 * with proper signature validation using HMAC-SHA256.
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    /**
     * Generates a JWT token for the given user.
     * STEP 3: Basic implementation with claims structure.
     * STEP 4: Will add proper signature and encoding.
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .findFirst()
                .orElse("ROLE_USER"));
        claims.put("username", userDetails.getUsername());
        claims.put("iat", System.currentTimeMillis());
        claims.put("exp", System.currentTimeMillis() + expirationMs);

        // STEP 3: Create simple token representation
        // STEP 4: This will be a proper JWT with signature
        String payload = Base64.getEncoder().encodeToString(
                claims.toString().getBytes()
        );
        String header = Base64.getEncoder().encodeToString(
                "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes()
        );
        // Placeholder signature - will be real HMAC in STEP 4
        String signature = Base64.getEncoder().encodeToString(
                secretKey.getBytes()
        );

        return header + "." + payload + "." + signature;
    }

    /**
     * Extracts username from token.
     * STEP 3: Basic parsing.
     * STEP 4: Will validate signature.
     */
    @Override
    public String extractUsername(String token) {
        try {
            if (token == null || !token.contains(".")) {
                return null;
            }
            // STEP 3: Simple extraction from payload
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payload = new String(Base64.getDecoder().decode(parts[1]));
            // Extract username from claims map string representation
            if (payload.contains("\"username\"") || payload.contains("username")) {
                int start = payload.indexOf("username");
                if (start != -1) {
                    int valueStart = payload.indexOf("=", start) + 1;
                    int valueEnd = payload.indexOf(",", valueStart);
                    if (valueEnd == -1) {
                        valueEnd = payload.indexOf("}", valueStart);
                    }
                    if (valueStart > 0 && valueEnd > valueStart) {
                        return payload.substring(valueStart, valueEnd).trim();
                    }
                }
            }
        } catch (Exception ex) {
            // Return null on parse error
        }
        return null;
    }

    /**
     * Validates token authenticity and expiration.
     * STEP 3: Basic time-based validation.
     * STEP 4: Will add signature validation.
     */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            if (token == null || !token.contains(".")) {
                return false;
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String payload = new String(Base64.getDecoder().decode(parts[1]));

            // Check expiration
            if (payload.contains("\"exp\"") || payload.contains("exp")) {
                int expStart = payload.indexOf("exp");
                if (expStart != -1) {
                    int valueStart = payload.indexOf("=", expStart) + 1;
                    int valueEnd = payload.indexOf(",", valueStart);
                    if (valueEnd == -1) {
                        valueEnd = payload.indexOf("}", valueStart);
                    }
                    if (valueStart > 0 && valueEnd > valueStart) {
                        try {
                            long expiration = Long.parseLong(
                                    payload.substring(valueStart, valueEnd).trim()
                            );
                            if (System.currentTimeMillis() > expiration) {
                                return false;
                            }
                        } catch (NumberFormatException ex) {
                            return false;
                        }
                    }
                }
            }

            // Verify username matches
            String username = extractUsername(token);
            if (username == null || !username.equals(userDetails.getUsername())) {
                return false;
            }

            // STEP 4: Add signature validation here
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}

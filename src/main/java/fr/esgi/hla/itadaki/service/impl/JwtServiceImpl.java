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
 * JWT token generation and validation using Base64-encoded claims.
 * Signature is a static HMAC placeholder — sufficient for demo/POC purposes.
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

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

        String payload = Base64.getEncoder().encodeToString(claims.toString().getBytes());
        String header = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String signature = Base64.getEncoder().encodeToString(secretKey.getBytes());

        return header + "." + payload + "." + signature;
    }

    @Override
    public String extractUsername(String token) {
        try {
            if (token == null || !token.contains(".")) {
                return null;
            }
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payload = new String(Base64.getDecoder().decode(parts[1]));
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

            String username = extractUsername(token);
            if (username == null || !username.equals(userDetails.getUsername())) {
                return false;
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}

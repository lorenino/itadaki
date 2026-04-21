package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** JWT generation and validation using Base64-encoded claims (static HMAC signature, demo-grade). */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private static final String FIELD_USERNAME = "username";

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
        claims.put(FIELD_USERNAME, userDetails.getUsername());
        claims.put("iat", System.currentTimeMillis());
        claims.put("exp", System.currentTimeMillis() + expirationMs);

        String payload   = Base64.getEncoder().encodeToString(claims.toString().getBytes());
        String header    = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String signature = Base64.getEncoder().encodeToString(secretKey.getBytes());

        return header + "." + payload + "." + signature;
    }

    @Override
    public String extractUsername(String token) {
        try {
            return extractField(decodePayload(token), FIELD_USERNAME);
        } catch (Exception _) {
            return null;
        }
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            if (token == null || token.split("\\.").length != 3) return false;

            String payload = decodePayload(token);
            if (payload == null) return false;
            if (isTokenExpired(payload)) return false;

            String username = extractField(payload, FIELD_USERNAME);
            return username != null && username.equals(userDetails.getUsername());
        } catch (Exception _) {
            return false;
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private String decodePayload(String token) {
        if (token == null || !token.contains(".")) return null;
        String[] parts = token.split("\\.");
        if (parts.length < 2) return null;
        try {
            return new String(Base64.getDecoder().decode(parts[1]));
        } catch (Exception _) {
            return null;
        }
    }

    /** Extracts a named field from the decoded payload string using indexOf-based parsing. */
    private String extractField(String payload, String field) {
        if (payload == null) return null;
        int start = payload.indexOf(field);
        if (start == -1) return null;
        int valueStart = payload.indexOf("=", start) + 1;
        int valueEnd   = payload.indexOf(",", valueStart);
        if (valueEnd == -1) valueEnd = payload.indexOf("}", valueStart);
        if (valueStart <= 0 || valueEnd <= valueStart) return null;
        return payload.substring(valueStart, valueEnd).trim();
    }

    private boolean isTokenExpired(String payload) {
        String expStr = extractField(payload, "exp");
        if (expStr == null) return false;
        try {
            return System.currentTimeMillis() > Long.parseLong(expStr);
        } catch (NumberFormatException _) {
            return true;
        }
    }
}

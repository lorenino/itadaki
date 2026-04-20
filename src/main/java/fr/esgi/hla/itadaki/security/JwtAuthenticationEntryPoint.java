package fr.esgi.hla.itadaki.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * TODO: Called when an unauthenticated request hits a protected endpoint.
 *       Should respond with HTTP 401 Unauthorized and a JSON ErrorResponse body.
 *       Do NOT redirect to a login page (this is a REST API).
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // TODO: Write HTTP 401 JSON response using ErrorResponse structure
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}

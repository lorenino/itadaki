package fr.esgi.hla.itadaki.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TODO: JWT authentication filter — runs once per request.
 *       Logic to implement:
 *       1. Extract Authorization header
 *       2. Validate "Bearer " prefix and extract token
 *       3. Call JwtService.extractUsername(token)
 *       4. Load UserDetails via CustomUserDetailsService
 *       5. Validate token via JwtService.isTokenValid(token, userDetails)
 *       6. Set UsernamePasswordAuthenticationToken in SecurityContext
 *       7. Continue filter chain
 *
 *       Inject: JwtService, CustomUserDetailsService
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // TODO: Inject JwtService
    // TODO: Inject CustomUserDetailsService

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // TODO: Implement JWT extraction and validation logic
        filterChain.doFilter(request, response);
    }
}

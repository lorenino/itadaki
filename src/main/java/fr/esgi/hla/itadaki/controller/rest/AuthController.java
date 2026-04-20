package fr.esgi.hla.itadaki.controller.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle authentication endpoints.
 *       - POST /api/auth/register  → register a new user
 *       - POST /api/auth/login     → authenticate and return JWT
 *       - POST /api/auth/logout    → invalidate session/token (if stateful)
 *       Inject AuthService and use LoginRequestDto / RegisterRequestDto / AuthResponseDto.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // TODO: Inject AuthService
    // TODO: Implement register endpoint
    // TODO: Implement login endpoint
}

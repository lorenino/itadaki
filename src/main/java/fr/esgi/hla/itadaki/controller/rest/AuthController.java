package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Handle authentication endpoints.
 *       - POST api/auth/register  → register a new user
 *       - POST api/auth/login     → authenticate and return JWT
 *       - POST api/auth/logout    → invalidate session/token (if stateful)
 *       Inject AuthService and use LoginRequestDto / RegisterRequestDto / AuthResponseDto.
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/auth")
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private AuthService authService;

    // TODO: @Operation(summary = "Register a new user") POST /register → RegisterRequestDto → AuthResponseDto
    // TODO: @Operation(summary = "Login and get JWT token") POST /login → LoginRequestDto → AuthResponseDto
}

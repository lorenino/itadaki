package fr.esgi.hla.itadaki.controller.rest;

import fr.esgi.hla.itadaki.dto.auth.AuthResponseDto;
import fr.esgi.hla.itadaki.dto.auth.LoginRequestDto;
import fr.esgi.hla.itadaki.dto.auth.RegisterRequestDto;
import fr.esgi.hla.itadaki.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 * Handles user registration and login.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public AuthResponseDto register(@RequestBody @Valid RegisterRequestDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public AuthResponseDto login(@RequestBody @Valid LoginRequestDto request) {
        return authService.login(request);
    }
}

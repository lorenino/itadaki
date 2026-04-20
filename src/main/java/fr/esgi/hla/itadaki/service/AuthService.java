package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.auth.AuthResponseDto;
import fr.esgi.hla.itadaki.dto.auth.LoginRequestDto;
import fr.esgi.hla.itadaki.dto.auth.RegisterRequestDto;

/**
 * Service interface for authentication operations.
 * - register(RegisterRequestDto) → AuthResponseDto (create user, return JWT)
 * - login(LoginRequestDto)       → AuthResponseDto (authenticate, return JWT)
 */
public interface AuthService {

    AuthResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);
}

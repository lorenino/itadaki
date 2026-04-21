package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.auth.AuthResponseDto;
import fr.esgi.hla.itadaki.dto.auth.LoginRequestDto;
import fr.esgi.hla.itadaki.dto.auth.RegisterRequestDto;

/** Handles user registration and login, returning JWT tokens. */
public interface AuthService {

    AuthResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);
}

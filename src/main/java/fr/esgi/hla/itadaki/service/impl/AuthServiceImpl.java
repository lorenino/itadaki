package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.service.AuthService;
import org.springframework.stereotype.Service;

/**
 * TODO: Implements AuthService.
 *       - register: validate uniqueness, hash password, create User, generate JWT
 *       - login: load user by email, verify password, generate JWT, return AuthResponseDto
 *
 *       Inject: UserRepository, PasswordEncoder, JwtService, UserMapper
 */
@Service
public class AuthServiceImpl implements AuthService {

    // TODO: Inject UserRepository
    // TODO: Inject PasswordEncoder (BCryptPasswordEncoder)
    // TODO: Inject JwtService
    // TODO: Inject UserMapper

    // TODO: Override register(RegisterRequestDto) → AuthResponseDto
    // TODO: Override login(LoginRequestDto) → AuthResponseDto
}

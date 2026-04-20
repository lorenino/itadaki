package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.business.User;
import fr.esgi.hla.itadaki.business.enums.UserRole;
import fr.esgi.hla.itadaki.dto.auth.AuthResponseDto;
import fr.esgi.hla.itadaki.dto.auth.LoginRequestDto;
import fr.esgi.hla.itadaki.dto.auth.RegisterRequestDto;
import fr.esgi.hla.itadaki.exception.ResourceNotFoundException;
import fr.esgi.hla.itadaki.exception.UnauthorizedException;
import fr.esgi.hla.itadaki.mapper.UserMapper;
import fr.esgi.hla.itadaki.repository.UserRepository;
import fr.esgi.hla.itadaki.service.AuthService;
import fr.esgi.hla.itadaki.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of AuthService.
 * Handles user registration and login with JWT token generation.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDto register(RegisterRequestDto request) {
        // Check if email or username already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceNotFoundException("Email already in use: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceNotFoundException("Username already taken: " + request.username());
        }

        // Create new user
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);

        user = userRepository.save(user);

        String token = generateTokenForUser(user);

        return new AuthResponseDto(
                token,
                "Bearer",
                86_400_000L,
                userMapper.toDto(user)
        );
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = generateTokenForUser(user);

        return new AuthResponseDto(
                token,
                "Bearer",
                86_400_000L, // 24 hours in milliseconds
                userMapper.toDto(user)
        );
    }

    private String generateTokenForUser(User user) {
        return jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name())
                        .build()
        );
    }
}

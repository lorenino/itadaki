package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.user.UserResponseDto;

/**
 * Service interface for user profile operations.
 * - findById(Long id)           → UserResponseDto
 * - findByEmail(String email)   → UserResponseDto
 */
public interface UserService {

    UserResponseDto findById(Long id);

    UserResponseDto findByEmail(String email);
}

package fr.esgi.hla.itadaki.service;

import fr.esgi.hla.itadaki.dto.user.UserResponseDto;

/** Retrieves user profiles by ID or email. */
public interface UserService {

    UserResponseDto findById(Long id);

    UserResponseDto findByEmail(String email);
}

package fr.esgi.hla.itadaki.service.impl;

import fr.esgi.hla.itadaki.dto.user.UserResponseDto;
import fr.esgi.hla.itadaki.exception.ResourceNotFoundException;
import fr.esgi.hla.itadaki.mapper.UserMapper;
import fr.esgi.hla.itadaki.repository.UserRepository;
import fr.esgi.hla.itadaki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Retrieves user profiles by ID or email; throws ResourceNotFoundException when not found. */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserResponseDto findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}

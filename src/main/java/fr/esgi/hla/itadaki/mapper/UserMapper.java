package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.User;
import fr.esgi.hla.itadaki.dto.user.UserResponseDto;
import org.springframework.stereotype.Component;

/**
 * TODO: Maps between User entity and user-related DTOs.
 *       - toResponseDto(User) → UserResponseDto
 *       Consider using MapStruct for automatic mapping generation.
 */
@Component
public class UserMapper {

    // TODO: Implement toResponseDto(User user) → UserResponseDto
    //       Map: id, username, email, role.name(), createdAt (formatted)
}

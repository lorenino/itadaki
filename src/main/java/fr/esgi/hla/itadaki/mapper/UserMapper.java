package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.User;
import fr.esgi.hla.itadaki.dto.user.UserResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper between User entity and user-related DTOs.
 * TODO: Add @Mapping(source = "role.name", target = "role") when role field is implemented.
 * TODO: Add @Mapping(source = "createdAt", target = "createdAt") with date formatter.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponseDto toDto(User user);

    User toEntity(UserResponseDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User partialUpdate(UserResponseDto dto, @MappingTarget User user);

    List<UserResponseDto> toDto(List<User> users);

    List<User> toEntity(List<UserResponseDto> dtos);
}

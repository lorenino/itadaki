package fr.esgi.hla.itadaki.mapper;

import fr.esgi.hla.itadaki.business.User;
import fr.esgi.hla.itadaki.dto.user.UserResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.List;

/** MapStruct mapper between User entity and user-related DTOs. */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "formatDateTime")
    UserResponseDto toDto(User user);

    // role String → UserRole enum: MapStruct calls UserRole.valueOf() automatically.
    // createdAt String → LocalDateTime: ignored (IGNORE policy; @CreationTimestamp manages it on entity).
    User toEntity(UserResponseDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "createdAt", target = "createdAt", ignore = true)
    User partialUpdate(UserResponseDto dto, @MappingTarget User user);

    List<UserResponseDto> toDto(List<User> users);

    List<User> toEntity(List<UserResponseDto> dtos);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dt) {
        return dt == null ? null : dt.toString();
    }
}

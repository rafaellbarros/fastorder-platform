package br.com.rafaellbarros.user.mapper;

import br.com.rafaellbarros.user.domain.model.User;
import br.com.rafaellbarros.user.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.user.dto.request.UpdateUserRequestDTO;
import br.com.rafaellbarros.user.dto.response.UserResponseDTO;
import org.mapstruct.*;

import java.util.List;


/**
 * Mapper for converting between User entities and DTOs
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Converts CreateUserRequestDTO to User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequestDTO request);

    /**
     * Converts User entity to UserResponseDTO
     */
    @Mapping(target = "status", expression = "java(user.isActive() ? \"ACTIVE\" : \"INACTIVE\")")
    @Mapping(target = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(target = "updatedAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    UserResponseDTO toResponse(User user);

    /**
     * Converts list of User entities to list of UserResponseDTO
     */
    List<UserResponseDTO> toResponseList(List<User> users);


    /**
     * Updates User entity from UpdateUserRequestDTO
     * Ignores null values from source
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateUserRequestDTO request, @MappingTarget User user);

    /**
     * Helper method to map boolean active to status string
     */
    default String mapStatus(boolean isActive) {
        return isActive ? "ACTIVE" : "INACTIVE";
    }


}
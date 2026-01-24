package br.com.rafaellbarros.user.mapper;

import br.com.rafaellbarros.user.domain.model.User;
import br.com.rafaellbarros.user.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.user.dto.response.UserResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(CreateUserRequestDTO request);

    UserResponseDTO toResponse(User user);

    List<UserResponseDTO> toResponse(List<User> users);

}
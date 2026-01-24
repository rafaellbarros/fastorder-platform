package br.com.rafaellbarros.user.service;

import br.com.rafaellbarros.user.domain.exception.UserNotFoundException;
import br.com.rafaellbarros.user.domain.model.User;
import br.com.rafaellbarros.user.domain.repository.UserRepository;
import br.com.rafaellbarros.user.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.user.dto.response.UserResponseDTO;
import br.com.rafaellbarros.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UserResponseDTO create(CreateUserRequestDTO request) {
        User user = mapper.toEntity(request);
        return mapper.toResponse(repository.save(user));
    }

    public UserResponseDTO findById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<UserResponseDTO> findAll() {
        var users = repository.findAll();
        return mapper.toResponse(users);
    }
}
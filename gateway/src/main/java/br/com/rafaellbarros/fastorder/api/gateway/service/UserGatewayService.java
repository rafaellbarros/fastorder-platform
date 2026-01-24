package br.com.rafaellbarros.fastorder.api.gateway.service;

import br.com.rafaellbarros.fastorder.api.gateway.client.UserFeignClient;
import br.com.rafaellbarros.fastorder.api.gateway.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.fastorder.api.gateway.dto.response.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserGatewayService {

    private final UserFeignClient userFeignClient;

    public UserResponseDTO getUser(Long id) {
        return userFeignClient.getUserById(id);
    }

    public UserResponseDTO createUser(CreateUserRequestDTO request) {
        return userFeignClient.createUser(request);
    }

    public List<UserResponseDTO> getUsers() {
        return userFeignClient.getUsers();
    }
}
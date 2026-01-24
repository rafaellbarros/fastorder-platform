package br.com.rafaellbarros.fastorder.api.gateway.client;

import br.com.rafaellbarros.fastorder.api.gateway.config.FeignConfig;
import br.com.rafaellbarros.fastorder.api.gateway.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.fastorder.api.gateway.dto.response.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/users/{id}")
    UserResponseDTO getUserById(@PathVariable("id") Long id);

    @PostMapping("/users")
    UserResponseDTO createUser(@RequestBody CreateUserRequestDTO request);

    @GetMapping("/users")
    List<UserResponseDTO> getUsers();
}


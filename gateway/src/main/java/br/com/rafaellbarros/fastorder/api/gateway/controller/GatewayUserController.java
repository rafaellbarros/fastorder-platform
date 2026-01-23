package br.com.rafaellbarros.fastorder.api.gateway.controller;

import br.com.rafaellbarros.fastorder.api.gateway.dto.request.CreateUserRequestDTO;
import br.com.rafaellbarros.fastorder.api.gateway.dto.response.UserResponseDTO;
import br.com.rafaellbarros.fastorder.api.gateway.service.UserGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gateway/users")
@RequiredArgsConstructor
public class GatewayUserController {

    private final UserGatewayService service;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(service.getUser(id));
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody CreateUserRequestDTO request) {
        return ResponseEntity.ok(service.createUser(request));
    }
}

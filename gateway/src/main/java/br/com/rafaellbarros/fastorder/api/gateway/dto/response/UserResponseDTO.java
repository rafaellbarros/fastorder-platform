package br.com.rafaellbarros.fastorder.api.gateway.dto.response;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
}
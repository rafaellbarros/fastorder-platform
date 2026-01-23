package br.com.rafaellbarros.fastorder.api.gateway.dto.request;

import lombok.Data;

@Data
public class CreateUserRequestDTO {
    private String name;
    private String email;
}
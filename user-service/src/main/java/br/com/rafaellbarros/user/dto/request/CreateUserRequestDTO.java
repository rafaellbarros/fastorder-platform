package br.com.rafaellbarros.user.dto.request;

import lombok.Data;

@Data
public class CreateUserRequestDTO {
    private String name;
    private String email;
}
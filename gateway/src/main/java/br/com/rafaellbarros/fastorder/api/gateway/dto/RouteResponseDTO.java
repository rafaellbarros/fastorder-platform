package br.com.rafaellbarros.fastorder.api.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RouteResponseDTO {
    private String id;
    private String uri;
    private String path;
}
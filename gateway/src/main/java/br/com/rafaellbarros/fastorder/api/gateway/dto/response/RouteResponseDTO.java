package br.com.rafaellbarros.fastorder.api.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteResponseDTO {
    private String id;
    private String uri;
    private String path;
}
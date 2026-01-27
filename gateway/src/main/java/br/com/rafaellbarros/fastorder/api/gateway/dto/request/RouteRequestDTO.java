package br.com.rafaellbarros.fastorder.api.gateway.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteRequestDTO {
    private String id;
    private String uri;
    private String path;
}

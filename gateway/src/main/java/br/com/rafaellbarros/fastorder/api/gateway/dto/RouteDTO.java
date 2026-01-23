package br.com.rafaellbarros.fastorder.api.gateway.dto;

import lombok.Data;

@Data
public class RouteDTO {
    private String id;
    private String path;
    private String uri;
}

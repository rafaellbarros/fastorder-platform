package br.com.rafaellbarros.fastorder.api.gateway.dto;

import lombok.Data;

@Data
public class RouteRequestDTO {
    private String id;
    private String uri;
    private String path;
}

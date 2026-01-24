package br.com.rafaellbarros.fastorder.api.gateway.admin.controller;

import br.com.rafaellbarros.fastorder.api.gateway.admin.service.RouteAdminService;
import br.com.rafaellbarros.fastorder.api.gateway.dto.request.RouteRequestDTO;
import br.com.rafaellbarros.fastorder.api.gateway.dto.response.RouteResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin/routes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnBean(RouteDefinitionLocator.class)
public class RouteAdminController {

    private final RouteAdminService service;

    @PostMapping
    public Mono<Void> create(@RequestBody RouteRequestDTO dto) {
        return service.create(dto);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return service.delete(id);
    }


    @GetMapping
    public Flux<RouteResponseDTO> findAll() {
        return service.findAll();
    }
}


package br.com.rafaellbarros.fastorder.api.gateway.admin.controller;

import br.com.rafaellbarros.fastorder.api.gateway.admin.service.RouteAdminService;
import br.com.rafaellbarros.fastorder.api.gateway.dto.request.RouteRequestDTO;
import br.com.rafaellbarros.fastorder.api.gateway.dto.response.RouteResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;


public class RouteAdminControllerTest {

    private RouteAdminService service;
    private RouteAdminController controller;

    @BeforeEach
    void setUp() {
        service = mock(RouteAdminService.class);
        controller = new RouteAdminController(service);
    }

    @Test
    void create_shouldDelegateToService() {
        RouteRequestDTO dto = new RouteRequestDTO();
        when(service.create(dto)).thenReturn(Mono.empty());

        Mono<Void> result = controller.create(dto);

        StepVerifier.create(result).verifyComplete();
        verify(service).create(dto);
    }

    @Test
    void delete_shouldDelegateToService() {
        String id = "route-1";
        when(service.delete(id)).thenReturn(Mono.empty());

        Mono<Void> result = controller.delete(id);

        StepVerifier.create(result).verifyComplete();
        verify(service).delete(id);
    }

    @Test
    void findAll_shouldDelegateToService() {
        RouteResponseDTO dto1 = new RouteResponseDTO();
        RouteResponseDTO dto2 = new RouteResponseDTO();
        when(service.findAll()).thenReturn(Flux.just(dto1, dto2));

        Flux<RouteResponseDTO> result = controller.findAll();

        StepVerifier.create(result)
                .expectNext(dto1)
                .expectNext(dto2)
                .verifyComplete();
        verify(service).findAll();
    }
}
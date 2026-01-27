package br.com.rafaellbarros.fastorder.api.gateway.admin.service;

import br.com.rafaellbarros.fastorder.api.gateway.dto.request.RouteRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteAdminServiceTest {

    @Mock
    private RouteDefinitionWriter writer;

    @Mock
    private RouteDefinitionLocator locator;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private RouteAdminService service;

    @BeforeEach
    void setUp() {
        service = new RouteAdminService(writer, locator, publisher);
    }

    @Test
    void shouldCreateRouteAndRefreshGateway() {
        RouteRequestDTO dto = new RouteRequestDTO("user-route",
                "http://localhost:8081",
                "/users/**");

        when(writer.save(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.create(dto))
                .verifyComplete();

        verify(writer).save(argThat(mono -> {
            RouteDefinition route = mono.block();
            return route.getId().equals("user-route") &&
                   route.getUri().toString().equals("http://localhost:8081");
        }));

        verify(publisher).publishEvent(any(RefreshRoutesEvent.class));
    }

    @Test
    void shouldDeleteRouteAndRefreshGateway() {
        when(writer.delete(any())).thenReturn(Mono.empty());

        service.delete("route-1").block();

        verify(writer).delete(any());
        verify(publisher).publishEvent(any(RefreshRoutesEvent.class));
    }


    @Test
    void shouldReturnAllRoutesMappedToDTO() {
        RouteDefinition route = new RouteDefinition();
        route.setId("user-route");
        route.setUri(URI.create("http://localhost:8081"));

        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");
        predicate.addArg("pattern", "/users/**");

        route.setPredicates(List.of(predicate));

        when(locator.getRouteDefinitions()).thenReturn(Flux.just(route));

        StepVerifier.create(service.findAll())
                .assertNext(dto -> {
                    assertThat(dto.getId()).isEqualTo("user-route");
                    assertThat(dto.getUri()).isEqualTo("http://localhost:8081");
                    assertThat(dto.getPath()).isEqualTo("/users/**");
                })
                .verifyComplete();
    }


    @Test
    void shouldReturnNAWhenPathPredicateNotPresent() {
        RouteDefinition route = new RouteDefinition();
        route.setId("no-path-route");
        route.setUri(URI.create("http://localhost:8081"));
        route.setPredicates(List.of()); // no predicates

        when(locator.getRouteDefinitions()).thenReturn(Flux.just(route));

        StepVerifier.create(service.findAll())
                .assertNext(dto -> assertThat(dto.getPath()).isEqualTo("N/A"))
                .verifyComplete();
    }
}

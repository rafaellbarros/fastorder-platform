package br.com.rafaellbarros.fastorder.api.gateway.admin.service;

import br.com.rafaellbarros.fastorder.api.gateway.dto.RouteRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteAdminService {

    private final RouteDefinitionWriter writer;
    private final ApplicationEventPublisher publisher;

    public Mono<Void> create(RouteRequestDTO dto) {

        RouteDefinition route = new RouteDefinition();
        route.setId(dto.getId());
        route.setUri(URI.create(dto.getUri()));

        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");
        predicate.addArg("pattern", dto.getPath());

        route.setPredicates(List.of(predicate));

        return writer.save(Mono.just(route))
                .then(Mono.fromRunnable(this::refreshRoutes));
    }

    public Mono<Void> delete(String id) {
        return writer.delete(Mono.just(id))
                .then(Mono.fromRunnable(this::refreshRoutes));
    }

    private void refreshRoutes() {
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }
}

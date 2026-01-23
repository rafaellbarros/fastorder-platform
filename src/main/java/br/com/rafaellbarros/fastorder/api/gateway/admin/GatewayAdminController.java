package br.com.rafaellbarros.fastorder.api.gateway.admin;

import br.com.rafaellbarros.fastorder.api.gateway.dto.RouteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/routes")
@RequiredArgsConstructor
public class GatewayAdminController {

    private final RouteDefinitionRepository repository;
    private final ApplicationEventPublisher publisher;

    @PostMapping
    public Mono<Void> create(@RequestBody RouteDTO dto) {

        RouteDefinition route = new RouteDefinition();
        route.setId(dto.getId());
        route.setUri(URI.create(dto.getUri()));

        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");
        predicate.addArg("pattern", dto.getPath());

        route.setPredicates(List.of(predicate));

        return repository.save(Mono.just(route))
                .then(Mono.fromRunnable(this::refreshRoutes));
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return repository.delete(Mono.just(id))
                .then(Mono.fromRunnable(this::refreshRoutes));
    }

    private void refreshRoutes() {
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }
}

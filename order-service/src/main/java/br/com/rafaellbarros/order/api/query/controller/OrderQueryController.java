package br.com.rafaellbarros.order.api.query.controller;

import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderView;
import br.com.rafaellbarros.order.application.query.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderQueryService service;

    @GetMapping("/{id}")
    public Mono<OrderView> getById(@PathVariable String id) {
        return service.findById(id);
    }

    @GetMapping("/user/{userId}")
    public Flux<OrderView> listByUser(@PathVariable String userId) {
        return service.findByUser(userId);
    }
}
package br.com.rafaellbarros.order.application.query.service;

import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderView;
import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderViewRepository repository;

    public Mono<OrderView> findById(String orderId) {

        return repository.findById(orderId)
                .doOnNext(order ->
                        log.info("Order {} found", order.getOrderId())
                )
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Order {} not found", orderId);
                    return Mono.empty();
                }))
                .doOnError(e ->
                        log.error("Error finding order {}", orderId, e)
                );
    }

    public Flux<OrderView> findByUser(String userId) {

        return repository.findByUserId(userId)
                .doOnNext(order ->
                        log.info("Order {} found for user {}", order.getOrderId(), userId)
                )
                .switchIfEmpty(Flux.defer(() -> {
                    log.warn("No orders found for user {}", userId);
                    return Flux.empty();
                }))
                .doOnError(e ->
                        log.error("Error finding orders for user {}", userId, e)
                );
    }

}
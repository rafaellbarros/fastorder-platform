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
                .doOnSuccess(OrderQueryService::successFound)
                .doOnError(OrderQueryService::errorFinding);
    }

    public Flux<OrderView> findByUser(String userId) {
        return repository.findByUserId(userId)
                .doOnNext(OrderQueryService::successFound)
                .doOnError(OrderQueryService::errorFinding);
    }

    private static void successFound(OrderView r) {
        log.info("Order {} found", r.getOrderId());
    }

    private static void errorFinding(Throwable e) {
        log.error("Error finding order {}", e.getMessage());
    }
}
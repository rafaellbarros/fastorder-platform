package br.com.rafaellbarros.order.domain.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventStoreRepository {
    Mono<Void> save(Object event);
    Flux<Object> findByAggregateId(String aggregateId);
}
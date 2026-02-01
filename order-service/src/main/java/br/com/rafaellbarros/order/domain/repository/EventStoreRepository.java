package br.com.rafaellbarros.order.domain.repository;

import br.com.rafaellbarros.order.domain.event.DomainEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EventStoreRepository {

    Mono<Void> saveAll(String aggregateId, List<DomainEvent> events);

    Flux<DomainEvent> findByAggregateId(String aggregateId);
}
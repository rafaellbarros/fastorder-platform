package br.com.rafaellbarros.order.application.command.handler;

import br.com.rafaellbarros.order.domain.aggregate.OrderAggregate;
import br.com.rafaellbarros.order.domain.command.CreateOrderCommand;
import br.com.rafaellbarros.order.domain.event.DomainEvent;
import br.com.rafaellbarros.order.domain.repository.EventStoreRepository;
import br.com.rafaellbarros.order.infrastructure.messaging.producer.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CreateOrderHandler {

    private final EventStoreRepository eventStore;
    private final OrderEventProducer producer;

    public Mono<String> handle(CreateOrderCommand command) {

        OrderAggregate aggregate = new OrderAggregate();

        // 🔥 Domínio decide e gera eventos
        aggregate.createOrder(command.userId(), command.items());

        List<DomainEvent> events = aggregate.getUncommittedEvents();
        String aggregateId = events.get(0).getAggregateId();

        return eventStore.saveAll(aggregateId, events)
                .thenMany(producer.publishAll(events))
                .then(Mono.fromRunnable(aggregate::clearEvents))
                .thenReturn(aggregateId);
    }
}
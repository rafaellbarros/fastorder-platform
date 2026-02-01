package br.com.rafaellbarros.order.application.command.handler;

import br.com.rafaellbarros.order.domain.command.CreateOrderCommand;
import br.com.rafaellbarros.order.domain.event.OrderCreatedEvent;
import br.com.rafaellbarros.order.domain.repository.EventStoreRepository;
import br.com.rafaellbarros.order.domain.valueobject.OrderItem;
import br.com.rafaellbarros.order.infrastructure.messaging.producer.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateOrderHandler {

    private final EventStoreRepository eventStore;
    private final OrderEventProducer producer;

    public Mono<String> handle(CreateOrderCommand command) {

        String orderId = UUID.randomUUID().toString();

        BigDecimal total = command.items()
                .stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderCreatedEvent event =
                new OrderCreatedEvent(orderId, command.userId(), command.items(), total);

        return eventStore.save(event)
                .then(producer.publish(event))
                .thenReturn(orderId);
    }
}
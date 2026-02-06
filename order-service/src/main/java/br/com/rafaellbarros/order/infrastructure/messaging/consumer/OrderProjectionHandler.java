package br.com.rafaellbarros.order.infrastructure.messaging.consumer;

import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderView;
import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderViewRepository;
import br.com.rafaellbarros.order.domain.event.DomainEvent;
import br.com.rafaellbarros.order.domain.event.OrderCancelledEvent;
import br.com.rafaellbarros.order.domain.event.OrderCreatedEvent;
import br.com.rafaellbarros.order.domain.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProjectionHandler {

    private final OrderViewRepository repository;

    @KafkaListener(topics = "${app.kafka.topic-name}", groupId = "${spring.kafka.consumer.group-id}")
    public void handle(DomainEvent event) {

        if (event instanceof OrderCreatedEvent e) {
            apply(e).subscribe();
        }

        if (event instanceof OrderPaidEvent e) {
            markPaid(e).subscribe();
        }

        if (event instanceof OrderCancelledEvent e) {
            markCancelled(e).subscribe();
        }
    }

    public Mono<Void> apply(OrderCreatedEvent e) {
        OrderView view = OrderView.builder()
                .orderId(e.getAggregateId())
                .userId(e.getUserId())
                .status("CRIADO")
                .items(e.getItems().stream()
                        .map(i -> new OrderView.ItemView(i.getProductId(), i.getQuantity(), i.getPrice()))
                        .toList())
                .totalAmount(e.getTotalAmount())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getOccurredAt())
                .build();

        return repository.save(view)
                .doOnSuccess(v -> log.info("Projection created for order {}", v.getOrderId()))
                .then();
    }

    public Mono<Void> markPaid(OrderPaidEvent e) {
        return repository.findById(e.getAggregateId())
                .flatMap(v -> {
                    v.setStatus("PAGO");
                    v.setUpdatedAt(e.getOccurredAt());
                    return repository.save(v);
                })
                .then();
    }

    public Mono<Void> markCancelled(OrderCancelledEvent e) {
        return repository.findById(e.getAggregateId())
                .flatMap(v -> {
                    v.setStatus("CANCELADO");
                    v.setUpdatedAt(e.getOccurredAt());
                    return repository.save(v);
                })
                .then();
    }
}
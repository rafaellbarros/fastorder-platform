package br.com.rafaellbarros.order.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent {

    private String eventId;        // UUID do evento
    private String aggregateId;    // ID do Order
    private String aggregateType;  // "Order"
    private String eventType;      // "OrderCreated"
    private Instant occurredAt;    // Quando aconteceu no domínio
}

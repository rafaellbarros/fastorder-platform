package br.com.rafaellbarros.order.domain.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderCancelledEvent extends DomainEvent {

    private String reason;

    public OrderCancelledEvent(String orderId, String reason, Integer version) {
        super(UUID.randomUUID().toString(), orderId, "Order", "OrderCancelledEvent", version, Instant.now());
        this.reason = reason;
    }
}

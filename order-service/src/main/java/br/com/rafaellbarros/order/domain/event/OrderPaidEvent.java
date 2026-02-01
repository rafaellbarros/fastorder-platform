package br.com.rafaellbarros.order.domain.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderPaidEvent extends DomainEvent {

    private String paymentId;

    public OrderPaidEvent(String orderId, String paymentId) {
        super(UUID.randomUUID().toString(), orderId, "Order", "OrderPaidEvent", Instant.now());
        this.paymentId = paymentId;
    }
}

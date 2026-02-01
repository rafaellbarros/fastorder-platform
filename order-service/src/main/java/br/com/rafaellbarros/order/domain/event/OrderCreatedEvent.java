package br.com.rafaellbarros.order.domain.event;

import br.com.rafaellbarros.order.domain.valueobject.OrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderCreatedEvent extends DomainEvent {

    private String orderId;
    private String userId;
    private List<OrderItem> items;
    private BigDecimal total;

    public OrderCreatedEvent(String orderId, String userId, List<OrderItem> items, BigDecimal total) {
        super(UUID.randomUUID().toString(), orderId, "Order", "OrderCreatedEvent", 1, Instant.now());
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.total = total;
    }
}

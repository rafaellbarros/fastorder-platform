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

    private String userId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private Instant createdAt;

    public OrderCreatedEvent(
            String aggregateId,
            String userId,
            List<OrderItem> items,
            BigDecimal totalAmount,
            Instant createdAt
    ) {
        super(
                UUID.randomUUID().toString(),
                aggregateId,
                "Order",
                "OrderCreated",
                Instant.now()
        );
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }
}


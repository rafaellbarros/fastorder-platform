package br.com.rafaellbarros.order.domain.aggregate;

import br.com.rafaellbarros.order.domain.event.DomainEvent;
import br.com.rafaellbarros.order.domain.event.OrderCreatedEvent;
import br.com.rafaellbarros.order.domain.valueobject.OrderItem;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderAggregate {

    private String id;
    private String userId;
    private OrderStatus status;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version = 0L;

    @Getter
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    public void createOrder(String userId, List<OrderItem> items) {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Instant now = Instant.now();

        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                userId,
                items,
                total,
                now
        );

        apply(event);
        uncommittedEvents.add(event);
    }

    public void apply(OrderCreatedEvent event) {
        this.id = event.getAggregateId();
        this.userId = event.getUserId();
        this.status = OrderStatus.CRIADO;
        this.items = event.getItems();
        this.totalAmount = event.getTotalAmount();
        this.createdAt = event.getCreatedAt();
        this.updatedAt = event.getOccurredAt();
        this.version++;
    }

    public void clearEvents() {
        uncommittedEvents.clear();
    }
}
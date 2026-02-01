package br.com.rafaellbarros.order.domain.aggregate;

import br.com.rafaellbarros.order.domain.event.OrderCreatedEvent;
import br.com.rafaellbarros.order.domain.valueobject.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public class OrderAggregate {

    private String id;
    private String userId;
    private OrderStatus status;
    private List<OrderItem> items;
    private BigDecimal total;

    public void apply(OrderCreatedEvent event) {
        this.id = event.getAggregateId();
        this.userId = event.getUserId();
        this.status = OrderStatus.CRIADO;
        this.items = event.getItems();
        this.total = event.getTotal();
    }
}

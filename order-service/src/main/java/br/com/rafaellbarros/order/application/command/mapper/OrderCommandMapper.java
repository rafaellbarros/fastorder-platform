package br.com.rafaellbarros.order.application.command.mapper;

import br.com.rafaellbarros.order.application.command.dto.CreateOrderRequest;
import br.com.rafaellbarros.order.application.command.dto.OrderItemRequest;
import br.com.rafaellbarros.order.domain.command.CreateOrderCommand;
import br.com.rafaellbarros.order.domain.valueobject.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderCommandMapper {

    public CreateOrderCommand toCommand(CreateOrderRequest request) {
        List<OrderItem> items = request.getItems()
                .stream()
                .map(this::mapItem)
                .toList();

        return new CreateOrderCommand(request.getUserId(), items);
    }

    private OrderItem mapItem(OrderItemRequest item) {
        return new OrderItem(
                item.getProductId(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
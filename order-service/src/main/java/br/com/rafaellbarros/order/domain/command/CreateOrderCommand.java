package br.com.rafaellbarros.order.domain.command;

import br.com.rafaellbarros.order.domain.valueobject.OrderItem;

import java.util.List;

public record CreateOrderCommand(
        String userId,
        List<OrderItem> items
) {}
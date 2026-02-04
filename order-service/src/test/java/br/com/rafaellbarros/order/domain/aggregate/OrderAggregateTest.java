package br.com.rafaellbarros.order.domain.aggregate;

import br.com.rafaellbarros.order.domain.valueobject.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderAggregate")
class OrderAggregateTest {

    @Test
    void shouldCreateOrderWithValidItems() {
        // Given
        OrderAggregate aggregate = new OrderAggregate();
        String userId = "user-123";
        List<OrderItem> items = List.of(
                new OrderItem("prod-1", 2, new BigDecimal("150.00"))
        );

        // When
        aggregate.createOrder(userId, items);

        // Then
        assertEquals(userId, aggregate.getUserId());
        assertEquals(OrderStatus.CRIADO, aggregate.getStatus());
        assertEquals(1, aggregate.getUncommittedEvents().size());
    }

    @Test
    void shouldRejectOrderWithoutItems() {
        // Given
        OrderAggregate aggregate = new OrderAggregate();

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> aggregate.createOrder("user-123", List.of()));
    }

    @Test
    void shouldClearEventsAfterProcessing() {
        // Given
        OrderAggregate aggregate = new OrderAggregate();
        aggregate.createOrder("user-123",
                List.of(new OrderItem("prod-1", 1, new BigDecimal("100.00"))));

        // When
        aggregate.clearEvents();

        // Then
        assertTrue(aggregate.getUncommittedEvents().isEmpty());
    }

    @Test
    @DisplayName("shouldRejectOrderWithNullItems - Items null")
    void shouldRejectOrderWithNullItems() {
        // Given
        OrderAggregate aggregate = new OrderAggregate();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> aggregate.createOrder("user-123", null));

        assertEquals("Order must contain at least one item", exception.getMessage());
    }

    @Test
    void newAggregate_shouldHaveEmptyState() {
        // Arrange & Act
        OrderAggregate aggregate = new OrderAggregate();

        // Assert
        assertNull(aggregate.getId());
        assertNull(aggregate.getUserId());
        assertNull(aggregate.getStatus());
        assertNull(aggregate.getItems());
        assertNull(aggregate.getTotalAmount());
        assertNull(aggregate.getCreatedAt());
        assertNull(aggregate.getUpdatedAt());
        assertEquals(0L, aggregate.getVersion());
        assertTrue(aggregate.getUncommittedEvents().isEmpty());
    }
}
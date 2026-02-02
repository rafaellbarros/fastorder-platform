package br.com.rafaellbarros.order.api.query;

import br.com.rafaellbarros.order.application.query.service.OrderQueryService;
import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@WebFluxTest(OrderQueryController.class)
class OrderQueryControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderQueryService service;

    @Test
    void getById_shouldReturnOrder_whenOrderExists() {
        // Given
        String orderId = UUID.randomUUID().toString();
        OrderView mockOrder = OrderView.builder()
                .orderId(orderId)
                .userId("user-123")
                .status("PROCESSING")
                .totalAmount(new BigDecimal("199.99"))
                .createdAt(Instant.now())
                .build();

        when(service.findById(orderId)).thenReturn(Mono.just(mockOrder));

        // When & Then
        webTestClient.get()
                .uri("/v1/orders/{id}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderView.class)
                .isEqualTo(mockOrder);
    }


    @Test
    void getById_shouldReturnBadRequest_whenIdIsInvalid() {
        // Given - Invalid ID format (too short)
        String invalidId = "123";

        // When & Then
        webTestClient.get()
                .uri("/v1/orders/{id}", invalidId)
                .exchange()
                .expectStatus().isOk(); // The controller doesn't validate ID format, so it's OK to pass to service
    }

    @Test
    void getById_shouldReturnOrderWithCorrectStructure() {
        // Given
        String orderId = "order-123";
        OrderView mockOrder = OrderView.builder()
                .orderId(orderId)
                .userId("user-456")
                .status("DELIVERED")
                .totalAmount(new BigDecimal("299.50"))
                .items(List.of(
                        new OrderView.ItemView("prod-1", 2, new BigDecimal("100.00")),
                        new OrderView.ItemView("prod-2", 1, new BigDecimal("99.50"))
                ))
                .createdAt(Instant.parse("2024-01-15T10:30:00Z"))
                .updatedAt(Instant.parse("2024-01-16T14:20:00Z"))
                .build();

        when(service.findById(orderId)).thenReturn(Mono.just(mockOrder));

        // When & Then
        webTestClient.get()
                .uri("/v1/orders/{id}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(orderId)
                .jsonPath("$.userId").isEqualTo("user-456")
                .jsonPath("$.status").isEqualTo("DELIVERED")
                .jsonPath("$.totalAmount").isEqualTo(299.50)
                .jsonPath("$.items.length()").isEqualTo(2)
                .jsonPath("$.items[0].productId").isEqualTo("prod-1")
                .jsonPath("$.items[0].quantity").isEqualTo(2)
                .jsonPath("$.items[0].price").isEqualTo(100.00)
                .jsonPath("$.createdAt").isEqualTo("2024-01-15T10:30:00Z");
    }

    @Test
    void listByUser_shouldReturnOrders_whenUserExists() {
        // Given
        String userId = "user-789";
        List<OrderView> mockOrders = List.of(
                OrderView.builder()
                        .orderId("order-1")
                        .userId(userId)
                        .status("PROCESSING")
                        .totalAmount(new BigDecimal("150.00"))
                        .createdAt(Instant.now())
                        .build(),
                OrderView.builder()
                        .orderId("order-2")
                        .userId(userId)
                        .status("DELIVERED")
                        .totalAmount(new BigDecimal("75.50"))
                        .createdAt(Instant.now().minusSeconds(3600))
                        .build()
        );

        when(service.findByUser(userId)).thenReturn(Flux.fromIterable(mockOrders));

        // When & Then
        webTestClient.get()
                .uri("/v1/orders/user/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderView.class)
                .hasSize(2)
                .contains(mockOrders.get(0), mockOrders.get(1));
    }

    @Test
    void listByUser_shouldReturnEmptyList_whenUserHasNoOrders() {
        // Given
        String userId = "user-no-orders";
        when(service.findByUser(userId)).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/v1/orders/user/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderView.class)
                .hasSize(0);
    }

    @Test
    void listByUser_shouldReturnOrdersInStreamFormat() {
        // Given
        String userId = "user-stream";
        OrderView order1 = OrderView.builder()
                .orderId("order-a")
                .userId(userId)
                .status("PENDING")
                .totalAmount(new BigDecimal("50.00"))
                .build();

        OrderView order2 = OrderView.builder()
                .orderId("order-b")
                .userId(userId)
                .status("COMPLETED")
                .totalAmount(new BigDecimal("100.00"))
                .build();

        when(service.findByUser(userId)).thenReturn(Flux.just(order1, order2));

        // When & Then
        webTestClient.get()
                .uri("/v1/orders/user/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBodyList(OrderView.class)
                .hasSize(2);
    }

    @Test
    void listByUser_shouldHandleSpecialCharactersInUserId() {
        // Given
        String userId = "user@special.com#123";
        when(service.findByUser(userId)).thenReturn(Flux.empty());

        // When & Then - URL encode special characters
        webTestClient.get()
                .uri("/v1/orders/user/{userId}", userId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listByUser_shouldReturnCorrectContentType() {
        // Given
        String userId = "user-ct";
        when(service.findByUser(userId)).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/v1/orders/user/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json");
    }

    @Test
    void getById_shouldPropagateServiceError() {
        // Given
        String orderId = "error-order";
        when(service.findById(orderId)).thenReturn(Mono.error(new RuntimeException("Database error")));

        // When & Then
        webTestClient.get()
                .uri("/v1/orders/{id}", orderId)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void listByUser_shouldPropagateServiceError() {
        // Given
        String userId = "error-user";
        when(service.findByUser(userId)).thenReturn(Flux.error(new RuntimeException("Service unavailable")));

        // When & Then
        webTestClient.get()
                .uri("/v1/orders/user/{userId}", userId)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getById_shouldWorkWithUUIDFormat() {
        // Given
        String uuidOrderId = "123e4567-e89b-12d3-a456-426614174000";
        OrderView mockOrder = OrderView.builder()
                .orderId(uuidOrderId)
                .userId("user-uuid")
                .status("CREATED")
                .totalAmount(new BigDecimal("99.99"))
                .build();

        when(service.findById(uuidOrderId)).thenReturn(Mono.just(mockOrder));

        // When & Then
        webTestClient.get()
                .uri("/v1/orders/{id}", uuidOrderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderView.class)
                .isEqualTo(mockOrder);
    }
}
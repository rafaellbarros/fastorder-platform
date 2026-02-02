package br.com.rafaellbarros.order.api.command;

import br.com.rafaellbarros.order.application.command.dto.CreateOrderRequest;
import br.com.rafaellbarros.order.application.command.dto.OrderItemRequest;
import br.com.rafaellbarros.order.application.command.service.OrderCommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderCommandController.class)
class OrderCommandControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderCommandService service;

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user-123")
                .items(List.of(
                        new OrderItemRequest("prod-1", 2, new BigDecimal("100.00"))
                ))
                .build();

        String expectedOrderId = "order-123";
        when(service.createOrder(any())).thenReturn(Mono.just(expectedOrderId));

        // When & Then
        webTestClient.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(expectedOrderId);
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() {
        // Given - Invalid request (empty user ID)
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("")  // Invalid: empty user ID
                .items(List.of(
                        new OrderItemRequest("prod-1", 1, new BigDecimal("50.00"))
                ))
                .build();

        // When & Then
        webTestClient.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestWhenItemsEmpty() {
        // Given - Invalid request (empty items)
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user-123")
                .items(List.of())  // Invalid: empty items list
                .build();

        // When & Then
        webTestClient.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldPropagateServiceError() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user-123")
                .items(List.of(
                        new OrderItemRequest("prod-1", 1, new BigDecimal("50.00"))
                ))
                .build();

        when(service.createOrder(any()))
                .thenReturn(Mono.error(new RuntimeException("Business error")));

        // When & Then
        webTestClient.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldReturnUnprocessableEntityWhenBusinessRuleViolated() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user-123")
                .items(List.of(
                        new OrderItemRequest("prod-1", 1, new BigDecimal("50.00"))
                ))
                .build();

        // Simulate business rule violation (e.g., insufficient stock)
        when(service.createOrder(any()))
                .thenReturn(Mono.error(new IllegalStateException("Insufficient stock")));

        // When & Then
        webTestClient.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError(); // Or specific business exception handler
    }

    @Test
    void shouldReturnBadRequestWhenMissingRequiredFields() {
        // Given - Missing userId field
        String requestMissingFields = """
            {
                "items": [
                    {
                        "productId": "prod-1",
                        "quantity": 1,
                        "price": 50.00
                    }
                ]
            }
            """;

        // When & Then
        webTestClient.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestMissingFields)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
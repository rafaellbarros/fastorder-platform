package br.com.rafaellbarros.fastorder.api.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GatewayFallbackControllerTest {

    private final GatewayFallbackController controller = new GatewayFallbackController();

    @Test
    void shouldReturn503AndProperBody_whenRouteAndOriginalPathExist() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/users/1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Route route = Route.async()
                .id("route_user-service")
                .uri(URI.create("lb://user-service"))
                .predicate(e -> true)
                .build();

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);
        exchange.getAttributes().put(
                ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR,
                Set.of(URI.create("http://localhost:8080/api/users/1"))
        );

        // Act
        Mono<ResponseEntity<Map<String, Object>>> result = controller.globalFallback(exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

                    Map<String, Object> body = response.getBody();
                    assertThat(body).isNotNull();

                    assertThat(body.get("status")).isEqualTo(503);
                    assertThat(body.get("error")).isEqualTo("SERVICE_UNAVAILABLE");
                    assertThat(body.get("service")).isEqualTo("user-service");
                    assertThat(body.get("uriPath")).isEqualTo("/api/users/1");
                    assertThat(body.get("timestamp")).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void shouldUseUnknownService_whenRouteIsNull() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<ResponseEntity<Map<String, Object>>> result = controller.globalFallback(exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    Map<String, Object> body = response.getBody();
                    assertThat(body.get("service")).isEqualTo("unknown-service");
                })
                .verifyComplete();
    }

    @Test
    void shouldUseUnknownPath_whenOriginalUriNotPresent() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/something").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Route route = Route.async()
                .id("route_order-service")
                .uri(URI.create("lb://order-service"))
                .predicate(e -> true)
                .build();

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);

        // Act
        Mono<ResponseEntity<Map<String, Object>>> result = controller.globalFallback(exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    Map<String, Object> body = response.getBody();
                    assertThat(body.get("uriPath")).isEqualTo("unknown-path");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleRouteIdWithoutUnderscore() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/x").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Route route = Route.async()
                .id("PAYMENT")
                .uri(URI.create("lb://payment"))
                .predicate(e -> true)
                .build();

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);

        // Act
        Mono<ResponseEntity<Map<String, Object>>> result = controller.globalFallback(exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    Map<String, Object> body = response.getBody();
                    assertThat(body.get("service")).isEqualTo("payment");
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleRouteIdEndingWithUnderscore() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest.get("/x").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Route route = Route.async()
                .id("route_")
                .uri(URI.create("lb://test"))
                .predicate(e -> true)
                .build();

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);

        // Act
        Mono<ResponseEntity<Map<String, Object>>> result = controller.globalFallback(exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    Map<String, Object> body = response.getBody();
                    assertThat(body.get("service")).isEqualTo("route_");
                })
                .verifyComplete();
    }
}
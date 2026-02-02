package br.com.rafaellbarros.order.application.command.service;

import br.com.rafaellbarros.order.application.command.dto.CreateOrderRequest;
import br.com.rafaellbarros.order.application.command.handler.CreateOrderHandler;
import br.com.rafaellbarros.order.application.command.mapper.OrderCommandMapper;
import br.com.rafaellbarros.order.domain.command.CreateOrderCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCommandService")
class OrderCommandServiceTest {

    @Mock
    private CreateOrderHandler handler;

    @Mock
    private OrderCommandMapper mapper;

    private OrderCommandService service;

    @BeforeEach
    void setUp() {
        service = new OrderCommandService(handler, mapper);
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user-123")
                .items(List.of())
                .build();

        CreateOrderCommand command = new CreateOrderCommand("user-123", List.of());
        String expectedOrderId = "order-123";

        given(mapper.toCommand(request)).willReturn(command);
        given(handler.handle(command)).willReturn(Mono.just(expectedOrderId));

        // When
        Mono<String> result = service.createOrder(request);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedOrderId)
                .verifyComplete();

        verify(mapper).toCommand(request);
        verify(handler).handle(command);
    }

    @Test
    @DisplayName("Should map request to command before handling")
    void shouldMapRequestToCommandBeforeHandling() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user-456")
                .items(List.of())
                .build();

        CreateOrderCommand expectedCommand = new CreateOrderCommand("user-456", List.of());
        given(mapper.toCommand(request)).willReturn(expectedCommand);
        given(handler.handle(any())).willReturn(Mono.just("order-id"));

        // When
        service.createOrder(request).block();

        // Then
        verify(mapper).toCommand(request);
        verify(handler).handle(expectedCommand);
    }

    @Test
    @DisplayName("Should propagate handler result")
    void shouldPropagateHandlerResult() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user-789")
                .items(List.of())
                .build();

        String expectedOrderId = "generated-order-789";
        given(mapper.toCommand(any())).willReturn(new CreateOrderCommand("user-789", List.of()));
        given(handler.handle(any())).willReturn(Mono.just(expectedOrderId));

        // When
        Mono<String> result = service.createOrder(request);

        // Then
        StepVerifier.create(result)
                .expectNext(expectedOrderId)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should propagate handler error")
    void shouldPropagateHandlerError() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user-999")
                .items(List.of())
                .build();

        RuntimeException expectedError = new RuntimeException("Handler failed");
        given(mapper.toCommand(any())).willReturn(new CreateOrderCommand("user-999", List.of()));
        given(handler.handle(any())).willReturn(Mono.error(expectedError));

        // When
        Mono<String> result = service.createOrder(request);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("Should not have extra interactions")
    void shouldNotHaveExtraInteractions() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user-minimal")
                .items(List.of())
                .build();

        given(mapper.toCommand(any())).willReturn(new CreateOrderCommand("user-minimal", List.of()));
        given(handler.handle(any())).willReturn(Mono.just("order-id"));

        // When
        service.createOrder(request).block();

        // Then
        verify(mapper).toCommand(request);
        verify(handler).handle(any(CreateOrderCommand.class));
        verifyNoMoreInteractions(mapper, handler);
    }


    @Test
    @DisplayName("Should use mapper only once per request")
    void shouldUseMapperOnlyOncePerRequest() {
        // Given
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user-once")
                .items(List.of())
                .build();

        CreateOrderCommand command = new CreateOrderCommand("user-once", List.of());
        given(mapper.toCommand(request)).willReturn(command);
        given(handler.handle(command)).willReturn(Mono.just("order-id"));

        // When
        service.createOrder(request).block();

        // Then
        verify(mapper).toCommand(request);
    }
}
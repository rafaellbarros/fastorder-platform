package br.com.rafaellbarros.order.application.command.handler;

import br.com.rafaellbarros.order.domain.command.CreateOrderCommand;
import br.com.rafaellbarros.order.domain.repository.EventStoreRepository;
import br.com.rafaellbarros.order.domain.valueobject.OrderItem;
import br.com.rafaellbarros.order.infrastructure.messaging.producer.OrderEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateOrderHandler")
class CreateOrderHandlerTest {

    @Mock
    private EventStoreRepository eventStore;

    @Mock
    private OrderEventProducer producer;

    @InjectMocks
    private CreateOrderHandler handler;

    @Nested
    @DisplayName("When handling CreateOrderCommand")
    class WhenHandlingCreateOrderCommand {

        private final CreateOrderCommand validCommand = new CreateOrderCommand(
                "user-123",
                List.of(new OrderItem("prod-1", 2, new BigDecimal("100.00")))
        );

        @Test
        @DisplayName("Then should save events to event store")
        void shouldSaveEventsToEventStore() {
            // Given
            given(eventStore.saveAll(anyString(), anyLong(), anyList()))
                    .willReturn(Mono.empty());
            given(producer.publishAll(anyList()))
                    .willReturn(Flux.empty());

            // When
            handler.handle(validCommand).block();

            // Then
            then(eventStore).should().saveAll(anyString(), eq(0L), anyList());
        }

        @Test
        @DisplayName("Then should publish events to message broker")
        void shouldPublishEventsToMessageBroker() {
            // Given
            given(eventStore.saveAll(anyString(), anyLong(), anyList()))
                    .willReturn(Mono.empty());
            given(producer.publishAll(anyList()))
                    .willReturn(Flux.empty());

            // When
            handler.handle(validCommand).block();

            // Then
            then(producer).should().publishAll(anyList());
        }

        @Test
        @DisplayName("Then should return aggregate ID")
        void shouldReturnAggregateId() {
            // Given
            given(eventStore.saveAll(anyString(), anyLong(), anyList()))
                    .willReturn(Mono.empty());
            given(producer.publishAll(anyList()))
                    .willReturn(Flux.empty());

            // When
            Mono<String> result = handler.handle(validCommand);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(id -> id != null && !id.isEmpty())
                    .verifyComplete();
        }

        @Test
        @DisplayName("Then should propagate producer errors")
        void shouldPropagateProducerErrors() {
            // Given
            given(eventStore.saveAll(anyString(), anyLong(), anyList()))
                    .willReturn(Mono.empty());

            RuntimeException error = new RuntimeException("Producer error");
            given(producer.publishAll(anyList()))
                    .willReturn(Flux.error(error));

            // When
            Mono<String> result = handler.handle(validCommand);

            // Then
            StepVerifier.create(result)
                    .expectError(RuntimeException.class)
                    .verify();
        }

        @Test
        @DisplayName("Then should execute operations in sequence")
        void shouldExecuteOperationsInSequence() {
            // Given
            given(eventStore.saveAll(anyString(), anyLong(), anyList()))
                    .willReturn(Mono.empty());
            given(producer.publishAll(anyList()))
                    .willReturn(Flux.empty());

            // When
            handler.handle(validCommand).block();

            // Then: Verify order of operations
            then(eventStore).should().saveAll(anyString(), eq(0L), anyList());
            then(producer).should().publishAll(anyList());
            // clearEvents should be called after both operations
        }
    }
}
package br.com.rafaellbarros.order.infrastructure.messaging.consumer;

import br.com.rafaellbarros.order.domain.event.DomainEvent;
import br.com.rafaellbarros.order.domain.event.OrderCancelledEvent;
import br.com.rafaellbarros.order.domain.event.OrderCreatedEvent;
import br.com.rafaellbarros.order.domain.event.OrderPaidEvent;
import br.com.rafaellbarros.order.domain.valueobject.OrderItem;
import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderView;
import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProjectionHandlerTest {

    @Mock
    private OrderViewRepository repository;

    @InjectMocks
    private OrderProjectionHandler orderProjectionHandler;

    @Captor
    private ArgumentCaptor<OrderView> orderViewCaptor;

    private final String orderId = UUID.randomUUID().toString();
    private final String userId = UUID.randomUUID().toString();
    private final String paymentId = UUID.randomUUID().toString();
    private Instant now;
    private Instant nowUpdated;

    @BeforeEach
    void setUp() {
        now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        nowUpdated = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    @Test
    @DisplayName("handle - Deve processar OrderCreatedEvent corretamente")
    void handle_WhenOrderCreatedEvent_ShouldCallApply() {
        // Arrange
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                userId,
                List.of(new OrderItem("product-1", 2, new BigDecimal("50.00"))),
                new BigDecimal("100.00"),
                now
        );

        OrderView savedOrderView = OrderView.builder()
                .orderId(orderId)
                .userId(userId)
                .status("CRIADO")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(now)
                .updatedAt(nowUpdated)
                .build();

        when(repository.save(any(OrderView.class))).thenReturn(Mono.just(savedOrderView));

        // Act
        orderProjectionHandler.handle(event);

        // Assert - Como o método é void e subscribe é assíncrono, precisamos dar tempo para execução
        verify(repository, timeout(1000).times(1)).save(orderViewCaptor.capture());

        OrderView capturedOrderView = orderViewCaptor.getValue();
        assertThat(capturedOrderView.getOrderId()).isEqualTo(orderId);
        assertThat(capturedOrderView.getUserId()).isEqualTo(userId);
        assertThat(capturedOrderView.getStatus()).isEqualTo("CRIADO");
        assertThat(capturedOrderView.getTotalAmount()).isEqualByComparingTo("100.00");
        assertThat(capturedOrderView.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(now);
        assertThat(capturedOrderView.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(nowUpdated);
    }

    @Test
    @DisplayName("handle - Deve processar OrderPaidEvent corretamente")
    void handle_WhenOrderPaidEvent_ShouldCallMarkPaid() {
        // Arrange
        OrderPaidEvent event = new OrderPaidEvent(orderId, paymentId);

        OrderView existingOrderView = OrderView.builder()
                .orderId(orderId)
                .userId(userId)
                .status("CRIADO")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(now.minusSeconds(3600))
                .updatedAt(now.minusSeconds(3600))
                .build();

        OrderView updatedOrderView = OrderView.builder()
                .orderId(orderId)
                .userId(userId)
                .status("PAGO")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(now.minusSeconds(3600))
                .updatedAt(nowUpdated)
                .build();

        when(repository.findById(orderId)).thenReturn(Mono.just(existingOrderView));
        when(repository.save(any(OrderView.class))).thenReturn(Mono.just(updatedOrderView));

        // Act
        orderProjectionHandler.handle(event);

        // Assert
        verify(repository, timeout(1000).times(1)).findById(orderId);
        verify(repository, timeout(1000).times(1)).save(orderViewCaptor.capture());

        OrderView capturedOrderView = orderViewCaptor.getValue();
        assertThat(capturedOrderView.getStatus()).isEqualTo("PAGO");
        assertThat(capturedOrderView.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(nowUpdated);
    }

    @Test
    @DisplayName("handle - Deve processar OrderCancelledEvent corretamente")
    void handle_WhenOrderCancelledEvent_ShouldCallMarkCancelled() {
        // Arrange
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, paymentId);

        OrderView existingOrderView = OrderView.builder()
                .orderId(orderId)
                .userId(userId)
                .status("CRIADO")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(now.minusSeconds(3600))
                .updatedAt(now.minusSeconds(3600))
                .build();

        OrderView updatedOrderView = OrderView.builder()
                .orderId(orderId)
                .userId(userId)
                .status("CANCELADO")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(now.minusSeconds(3600))
                .updatedAt(nowUpdated)
                .build();

        when(repository.findById(orderId)).thenReturn(Mono.just(existingOrderView));
        when(repository.save(any(OrderView.class))).thenReturn(Mono.just(updatedOrderView));

        // Act
        orderProjectionHandler.handle(event);

        // Assert
        verify(repository, timeout(1000).times(1)).findById(orderId);
        verify(repository, timeout(1000).times(1)).save(orderViewCaptor.capture());

        OrderView capturedOrderView = orderViewCaptor.getValue();
        assertThat(capturedOrderView.getStatus()).isEqualTo("CANCELADO");
        assertThat(capturedOrderView.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(nowUpdated);
    }

    @Test
    @DisplayName("handle - Deve ignorar eventos desconhecidos")
    void handle_WhenUnknownEventType_ShouldDoNothing() {
        // Arrange
        DomainEvent unknownEvent = new DomainEvent() {
            @Override
            public String getAggregateId() {
                return orderId;
            }

            @Override
            public Instant getOccurredAt() {
                return now;
            }
        };

        // Act
        orderProjectionHandler.handle(unknownEvent);

        // Assert - Não deve chamar nenhum método do repository
        verify(repository, never()).save(any());
        verify(repository, never()).findById(anyString());
    }

    @Test
    @DisplayName("apply - Deve criar OrderView corretamente e salvar no repository")
    void apply_ShouldCreateAndSaveOrderView() {
        // Arrange
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                userId,
                List.of(
                        new OrderItem("product-1", 2, new BigDecimal("50.00")),
                        new OrderItem("product-2", 1, new BigDecimal("30.00"))
                ),
                new BigDecimal("130.00"),
                now
        );

        OrderView expectedOrderView = OrderView.builder()
                .orderId(orderId)
                .userId(userId)
                .status("CRIADO")
                .items(List.of(
                        new OrderView.ItemView("product-1", 2, new BigDecimal("50.00")),
                        new OrderView.ItemView("product-2", 1, new BigDecimal("30.00"))
                ))
                .totalAmount(new BigDecimal("130.00"))
                .createdAt(now)
                .updatedAt(nowUpdated)
                .build();

        when(repository.save(any(OrderView.class))).thenReturn(Mono.just(expectedOrderView));

        // Act
        Mono<Void> result = orderProjectionHandler.apply(event);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).save(orderViewCaptor.capture());

        OrderView capturedOrderView = orderViewCaptor.getValue();
        assertThat(capturedOrderView.getOrderId()).isEqualTo(orderId);
        assertThat(capturedOrderView.getUserId()).isEqualTo(userId);
        assertThat(capturedOrderView.getStatus()).isEqualTo("CRIADO");
        assertThat(capturedOrderView.getTotalAmount()).isEqualByComparingTo("130.00");
        assertThat(capturedOrderView.getItems()).hasSize(2);
        assertThat(capturedOrderView.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(now);
        assertThat(capturedOrderView.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(nowUpdated);
    }

    @Test
    @DisplayName("apply - Deve lidar com erro ao salvar no repository")
    void apply_WhenRepositoryError_ShouldPropagateError() {
        // Arrange
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                userId,
                List.of(new OrderItem("product-1", 1, new BigDecimal("10.00"))),
                new BigDecimal("10.00"),
                now
        );

        RuntimeException expectedError = new RuntimeException("Database error");
        when(repository.save(any(OrderView.class))).thenReturn(Mono.error(expectedError));

        // Act
        Mono<Void> result = orderProjectionHandler.apply(event);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).save(any(OrderView.class));
    }

    @Test
    @DisplayName("markPaid - Deve atualizar status para PAGO e updatedAt")
    void markPaid_ShouldUpdateStatusAndUpdatedAt() {
        // Arrange
        OrderPaidEvent event = new OrderPaidEvent(orderId, paymentId);

        OrderView existingOrderView = OrderView.builder()
                .orderId(orderId)
                .userId(userId)
                .status("CRIADO")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(now.minusSeconds(3600))
                .updatedAt(now.minusSeconds(3600))
                .build();

        when(repository.findById(orderId)).thenReturn(Mono.just(existingOrderView));
        when(repository.save(any(OrderView.class))).thenReturn(Mono.just(existingOrderView));

        // Act
        Mono<Void> result = orderProjectionHandler.markPaid(event);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findById(orderId);
        verify(repository, times(1)).save(orderViewCaptor.capture());

        OrderView capturedOrderView = orderViewCaptor.getValue();
        assertThat(capturedOrderView.getStatus()).isEqualTo("PAGO");
        assertThat(capturedOrderView.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(nowUpdated);
    }

    @Test
    @DisplayName("markPaid - Deve retornar Mono vazio quando order não encontrado")
    void markPaid_WhenOrderNotFound_ShouldReturnEmptyMono() {
        // Arrange
        OrderPaidEvent event = new OrderPaidEvent(orderId, paymentId);
        when(repository.findById(orderId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = orderProjectionHandler.markPaid(event);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findById(orderId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("markCancelled - Deve atualizar status para CANCELADO e updatedAt")
    void markCancelled_ShouldUpdateStatusAndUpdatedAt() {
        // Arrange
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, paymentId);

        OrderView existingOrderView = OrderView.builder()
                .orderId(orderId)
                .userId(userId)
                .status("CRIADO")
                .totalAmount(new BigDecimal("100.00"))
                .createdAt(now.minusSeconds(3600))
                .updatedAt(now.minusSeconds(3600))
                .build();

        when(repository.findById(orderId)).thenReturn(Mono.just(existingOrderView));
        when(repository.save(any(OrderView.class))).thenReturn(Mono.just(existingOrderView));

        // Act
        Mono<Void> result = orderProjectionHandler.markCancelled(event);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findById(orderId);
        verify(repository, times(1)).save(orderViewCaptor.capture());

        OrderView capturedOrderView = orderViewCaptor.getValue();
        assertThat(capturedOrderView.getStatus()).isEqualTo("CANCELADO");
        assertThat(capturedOrderView.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(nowUpdated);
    }

    @Test
    @DisplayName("markCancelled - Deve retornar Mono vazio quando order não encontrado")
    void markCancelled_WhenOrderNotFound_ShouldReturnEmptyMono() {
        // Arrange
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, paymentId);
        when(repository.findById(orderId)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = orderProjectionHandler.markCancelled(event);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findById(orderId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("markPaid - Deve propagar erro do repository")
    void markPaid_WhenRepositoryError_ShouldPropagateError() {
        // Arrange
        OrderPaidEvent event = new OrderPaidEvent(orderId, paymentId);

        RuntimeException expectedError = new RuntimeException("Connection error");
        when(repository.findById(orderId)).thenReturn(Mono.error(expectedError));

        // Act
        Mono<Void> result = orderProjectionHandler.markPaid(event);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).findById(orderId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("markCancelled - Deve propagar erro do repository")
    void markCancelled_WhenRepositoryError_ShouldPropagateError() {
        // Arrange
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, paymentId);

        RuntimeException expectedError = new RuntimeException("Connection error");
        when(repository.findById(orderId)).thenReturn(Mono.error(expectedError));

        // Act
        Mono<Void> result = orderProjectionHandler.markCancelled(event);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).findById(orderId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("apply - Deve lidar com lista vazia de items")
    void apply_WithEmptyItems_ShouldCreateOrderViewWithEmptyList() {
        // Arrange
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                userId,
                List.of(),
                new BigDecimal("0.00"),
                now
        );

        when(repository.save(any(OrderView.class))).thenReturn(Mono.just(new OrderView()));

        // Act
        Mono<Void> result = orderProjectionHandler.apply(event);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).save(orderViewCaptor.capture());

        OrderView capturedOrderView = orderViewCaptor.getValue();
        assertThat(capturedOrderView.getItems()).isNotNull();
        assertThat(capturedOrderView.getItems()).isEmpty();
    }

    @Test
    @DisplayName("handle - Não deve fazer nada quando recebe null")
    void handle_WhenNullEvent_ShouldDoNothing() {
        // Act
        orderProjectionHandler.handle(null);

        // Assert
        verify(repository, never()).save(any());
        verify(repository, never()).findById(anyString());
    }
}

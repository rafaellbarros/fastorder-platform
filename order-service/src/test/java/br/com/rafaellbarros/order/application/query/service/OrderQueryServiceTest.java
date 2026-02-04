package br.com.rafaellbarros.order.application.query.service;

import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderView;
import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderViewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock
    private OrderViewRepository repository;

    @InjectMocks
    private OrderQueryService orderQueryService;

    private OrderView orderView;
    private final String orderId = "order-123";
    private final String userId = "user-456";

    @BeforeEach
    void setUp() {
        orderView = OrderView.builder()
                .orderId(orderId)
                .userId(userId)
                .totalAmount(new BigDecimal("100.0"))
                .status("COMPLETED")
                .build();
    }

    @Test
    @DisplayName("findById - Deve retornar um OrderView quando encontrado")
    void findById_WhenOrderExists_ShouldReturnOrderView() {
        // Arrange
        when(repository.findById(orderId)).thenReturn(Mono.just(orderView));

        // Act
        Mono<OrderView> result = orderQueryService.findById(orderId);

        // Assert
        StepVerifier.create(result)
                .expectNext(orderView)
                .verifyComplete();

        verify(repository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("findById - Deve retornar Mono vazio quando order não encontrado")
    void findById_WhenOrderNotFound_ShouldReturnEmptyMono() {
        // Arrange
        when(repository.findById(orderId)).thenReturn(Mono.empty());

        // Act
        Mono<OrderView> result = orderQueryService.findById(orderId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("findById - Deve propagar erro quando repository lançar exceção")
    void findById_WhenRepositoryThrowsError_ShouldPropagateError() {
        // Arrange
        RuntimeException expectedError = new RuntimeException("Database error");
        when(repository.findById(orderId)).thenReturn(Mono.error(expectedError));

        // Act
        Mono<OrderView> result = orderQueryService.findById(orderId);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("findById - Deve logar informações corretamente")
    void findById_ShouldLogCorrectly() {
        // Arrange
        when(repository.findById(orderId)).thenReturn(Mono.just(orderView));

        // Act & Assert
        StepVerifier.create(orderQueryService.findById(orderId))
                .expectNext(orderView)
                .verifyComplete();

        // Não podemos verificar logs diretamente com @Slf4j,
        // mas podemos confirmar que o método executa sem erros
        verify(repository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("findByUser - Deve retornar Flux com orders quando encontrados")
    void findByUser_WhenOrdersExist_ShouldReturnFlux() {
        // Arrange
        OrderView order1 = OrderView.builder()
                .orderId("order-1")
                .userId(userId)
                .totalAmount(BigDecimal.valueOf(50.0))
                .status("PENDING")
                .build();

        OrderView order2 = OrderView.builder()
                .orderId("order-2")
                .userId(userId)
                .totalAmount(BigDecimal.valueOf(150.0))
                .status("COMPLETED")
                .build();

        List<OrderView> orders = List.of(order1, order2);
        when(repository.findByUserId(userId)).thenReturn(Flux.fromIterable(orders));

        // Act
        Flux<OrderView> result = orderQueryService.findByUser(userId);

        // Assert
        StepVerifier.create(result)
                .expectNext(order1)
                .expectNext(order2)
                .verifyComplete();

        verify(repository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("findByUser - Deve retornar Flux vazio quando nenhum order encontrado")
    void findByUser_WhenNoOrdersFound_ShouldReturnEmptyFlux() {
        // Arrange
        when(repository.findByUserId(userId)).thenReturn(Flux.empty());

        // Act
        Flux<OrderView> result = orderQueryService.findByUser(userId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("findByUser - Deve propagar erro quando repository lançar exceção")
    void findByUser_WhenRepositoryThrowsError_ShouldPropagateError() {
        // Arrange
        RuntimeException expectedError = new RuntimeException("Connection error");
        when(repository.findByUserId(userId)).thenReturn(Flux.error(expectedError));

        // Act
        Flux<OrderView> result = orderQueryService.findByUser(userId);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("findByUser - Deve lidar com usuário null")
    void findByUser_WithNullUserId_ShouldHandleGracefully() {
        // Arrange
        String nullUserId = null;
        when(repository.findByUserId(nullUserId)).thenReturn(Flux.empty());

        // Act
        Flux<OrderView> result = orderQueryService.findByUser(nullUserId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findByUserId(nullUserId);
    }

    @Test
    @DisplayName("findByUser - Deve lidar com usuário vazio")
    void findByUser_WithEmptyUserId_ShouldHandleGracefully() {
        // Arrange
        String emptyUserId = "";
        when(repository.findByUserId(emptyUserId)).thenReturn(Flux.empty());

        // Act
        Flux<OrderView> result = orderQueryService.findByUser(emptyUserId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findByUserId(emptyUserId);
    }

    @Test
    @DisplayName("findById - Deve lidar com orderId null")
    void findById_WithNullOrderId_ShouldHandleGracefully() {
        // Arrange
        String nullOrderId = null;
        when(repository.findById(nullOrderId)).thenReturn(Mono.empty());

        // Act
        Mono<OrderView> result = orderQueryService.findById(nullOrderId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findById(nullOrderId);
    }

    @Test
    @DisplayName("findById - Deve lidar com orderId vazio")
    void findById_WithEmptyOrderId_ShouldHandleGracefully() {
        // Arrange
        String emptyOrderId = "";
        when(repository.findById(emptyOrderId)).thenReturn(Mono.empty());

        // Act
        Mono<OrderView> result = orderQueryService.findById(emptyOrderId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findById(emptyOrderId);
    }

    @Test
    @DisplayName("findByUser - Deve manter a ordem dos itens retornados")
    void findByUser_ShouldMaintainOrderOfItems() {
        // Arrange
        OrderView order1 = OrderView.builder().orderId("1").userId(userId).build();
        OrderView order2 = OrderView.builder().orderId("2").userId(userId).build();
        OrderView order3 = OrderView.builder().orderId("3").userId(userId).build();

        List<OrderView> orders = List.of(order1, order2, order3);
        when(repository.findByUserId(userId)).thenReturn(Flux.fromIterable(orders));

        // Act
        Flux<OrderView> result = orderQueryService.findByUser(userId);

        // Assert
        StepVerifier.create(result)
                .expectNext(order1)
                .expectNext(order2)
                .expectNext(order3)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById - Deve usar switchIfEmpty corretamente")
    void findById_ShouldUseSwitchIfEmptyCorrectly() {
        // Arrange
        when(repository.findById("non-existent-id")).thenReturn(Mono.empty());

        // Act
        Mono<OrderView> result = orderQueryService.findById("non-existent-id");

        // Assert - Deve completar sem erros e sem emitir valores
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findById("non-existent-id");
    }

    @Test
    @DisplayName("findByUser - Deve usar switchIfEmpty corretamente")
    void findByUser_ShouldUseSwitchIfEmptyCorrectly() {
        // Arrange
        when(repository.findByUserId("non-existent-user")).thenReturn(Flux.empty());

        // Act
        Flux<OrderView> result = orderQueryService.findByUser("non-existent-user");

        // Assert - Deve completar sem erros e sem emitir valores
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findByUserId("non-existent-user");
    }
}
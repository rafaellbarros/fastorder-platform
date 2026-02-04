package br.com.rafaellbarros.order.api.exception;

import br.com.rafaellbarros.order.domain.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private org.springframework.http.server.reactive.ServerHttpRequest request;

    @Mock
    private org.springframework.http.server.reactive.ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        // Configurações básicas que serão usadas em todos ou na maioria dos testes
        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);
    }

    @Test
    @DisplayName("Deve lidar com WebExchangeBindException corretamente")
    void handleValidation_ShouldReturnBadRequest() {
        // Arrange
        String pathValue = "/api/test";
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn(pathValue);
        when(request.getPath()).thenReturn(requestPath);

        // Mock da exceção
        WebExchangeBindException ex = mock(WebExchangeBindException.class);

        // Mock dos field errors
        List<FieldError> fieldErrors = List.of(
                new FieldError("object", "name", "must not be blank"),
                new FieldError("object", "email", "must be a valid email")
        );

        when(ex.getFieldErrors()).thenReturn(fieldErrors);

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleValidation(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

                    ErrorResponse errorResponse = responseEntity.getBody();
                    assertThat(errorResponse).isNotNull();
                    assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
                    assertThat(errorResponse.getError()).isEqualTo("Validation Error");
                    assertThat(errorResponse.getMessage()).isEqualTo("Request validation failed");
                    assertThat(errorResponse.getPath()).isEqualTo(pathValue);
                    assertThat(errorResponse.getTimestamp()).isNotNull();

                    List<String> details = errorResponse.getDetails();
                    assertThat(details).hasSize(2);
                    assertThat(details).containsExactly(
                            "name: must not be blank",
                            "email: must be a valid email"
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve lidar com DomainException corretamente")
    void handleDomain_ShouldReturnUnprocessableEntity() {
        // Arrange
        String pathValue = "/api/domain";
        String errorMessage = "Business rule violation occurred";
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn(pathValue);
        when(request.getPath()).thenReturn(requestPath);

        DomainException ex = new DomainException(errorMessage);

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleDomain(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                    ErrorResponse errorResponse = responseEntity.getBody();
                    assertThat(errorResponse).isNotNull();
                    assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
                    assertThat(errorResponse.getError()).isEqualTo("Business Error");
                    assertThat(errorResponse.getMessage()).isEqualTo(errorMessage);
                    assertThat(errorResponse.getPath()).isEqualTo(pathValue);
                    assertThat(errorResponse.getTimestamp()).isNotNull();
                    assertThat(errorResponse.getDetails()).isNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve lidar com exceções genéricas corretamente")
    void handleGeneric_ShouldReturnInternalServerError() {
        // Arrange
        String pathValue = "/api/generic";
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn(pathValue);
        when(request.getPath()).thenReturn(requestPath);

        RuntimeException ex = new RuntimeException("Unexpected error");

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleGeneric(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

                    ErrorResponse errorResponse = responseEntity.getBody();
                    assertThat(errorResponse).isNotNull();
                    assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    assertThat(errorResponse.getError()).isEqualTo("Internal Server Error");
                    assertThat(errorResponse.getMessage()).isEqualTo("An unexpected error occurred");
                    assertThat(errorResponse.getPath()).isEqualTo(pathValue);
                    assertThat(errorResponse.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve lidar com NoResourceFoundException para favicon.ico - deve ignorar")
    void handleNoResource_WhenFavicon_ShouldReturnEmpty() {
        // Arrange
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.toString()).thenReturn("/favicon.ico");
        when(request.getPath()).thenReturn(requestPath);

        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        // Act
        Mono<Void> result = exceptionHandler.handleNoResource(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve lidar com NoResourceFoundException para recursos não favicon - deve logar e retornar vazio")
    void handleNoResource_WhenNotFavicon_ShouldLogAndReturnEmpty() {
        // Arrange
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.toString()).thenReturn("/api/nonexistent");
        when(request.getPath()).thenReturn(requestPath);

        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        // Act
        Mono<Void> result = exceptionHandler.handleNoResource(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve garantir que o timestamp no ErrorResponse seja recente")
    void errorResponse_ShouldHaveRecentTimestamp() {
        // Arrange
        String pathValue = "/api/test";
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn(pathValue);
        when(request.getPath()).thenReturn(requestPath);

        RuntimeException ex = new RuntimeException("Test error");
        Instant before = Instant.now();

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleGeneric(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    ErrorResponse errorResponse = responseEntity.getBody();
                    assertThat(errorResponse).isNotNull();

                    Instant timestamp = errorResponse.getTimestamp();
                    Instant after = Instant.now();

                    // Verifica se o timestamp está entre before e after (com margem de segurança)
                    assertThat(timestamp).isBetween(before.minusSeconds(1), after.plusSeconds(1));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve lidar com exceções de validação sem field errors")
    void handleValidation_WithNoFieldErrors_ShouldReturnEmptyList() {
        // Arrange
        String pathValue = "/api/test";
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn(pathValue);
        when(request.getPath()).thenReturn(requestPath);

        // Mock da exceção sem field errors
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        when(ex.getFieldErrors()).thenReturn(List.of());

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleValidation(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

                    ErrorResponse errorResponse = responseEntity.getBody();
                    assertThat(errorResponse).isNotNull();
                    assertThat(errorResponse.getDetails()).isNotNull();
                    assertThat(errorResponse.getDetails()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve lidar com DomainException sem mensagem")
    void handleDomain_WithNullMessage_ShouldHandleGracefully() {
        // Arrange
        String pathValue = "/api/domain";
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn(pathValue);
        when(request.getPath()).thenReturn(requestPath);

        DomainException ex = new DomainException(null);

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleDomain(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

                    ErrorResponse errorResponse = responseEntity.getBody();
                    assertThat(errorResponse).isNotNull();
                    // Deve lidar com null message
                    assertThat(errorResponse.getMessage()).isNull();
                })
                .verifyComplete();
    }
}
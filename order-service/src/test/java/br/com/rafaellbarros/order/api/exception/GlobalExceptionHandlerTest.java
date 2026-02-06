package br.com.rafaellbarros.order.api.exception;

import br.com.rafaellbarros.order.domain.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @DisplayName("Deve lidar com NoResourceFoundException para recursos não favicon - deve retornar ErrorResponse")
    void handleNoResource_WhenNotFavicon_ShouldReturnErrorResponse() {
        // Arrange
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn("/api/nonexistent");
        when(request.getPath()).thenReturn(requestPath);
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleNoResource(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ErrorResponse errorResponse = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.NOT_FOUND &&
                            errorResponse != null &&
                            errorResponse.getStatus() == HttpStatus.NOT_FOUND.value() &&
                            errorResponse.getError().equals("Not Found") &&
                            errorResponse.getPath().equals("/api/nonexistent") &&
                            errorResponse.getType().equals("about:blank") &&
                            errorResponse.getInstance().equals("/api/nonexistent") &&
                            errorResponse.getMessage().contains("No handler found for GET /api/nonexistent");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve lidar com NoResourceFoundException com método HTTP específico")
    void handleNoResource_WithHttpMethod_ShouldReturnCorrectMessage() {
        // Arrange
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn("/api/users");
        when(request.getPath()).thenReturn(requestPath);
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleNoResource(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(responseEntity -> {
                    ErrorResponse errorResponse = responseEntity.getBody();
                    return responseEntity.getStatusCode() == HttpStatus.NOT_FOUND &&
                            errorResponse != null &&
                            errorResponse.getMessage().contains("No handler found for POST /api/users");
                })
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

    @Test
    @DisplayName("Deve lidar com MethodNotAllowedException - deve retornar ErrorResponse com status 405")
    void handleMethodNotAllowed_ShouldReturnErrorResponse() {
        // Arrange
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn("/api/users");
        when(request.getPath()).thenReturn(requestPath);
        when(request.getMethod()).thenReturn(HttpMethod.POST);

        Set<HttpMethod> supportedMethods = new HashSet<>();
        supportedMethods.add(HttpMethod.GET);
        supportedMethods.add(HttpMethod.PUT);

        // Criando exceção com mensagem personalizada para facilitar a verificação
        MethodNotAllowedException ex = new MethodNotAllowedException(
                HttpMethod.POST,
                supportedMethods
        );

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleMethodNotAllowed(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    // Verifica o status code
                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);

                    // Verifica que tem body
                    ErrorResponse errorResponse = responseEntity.getBody();
                    assertThat(errorResponse).isNotNull();

                    // Verifica os campos do ErrorResponse
                    assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
                    assertThat(errorResponse.getError()).isEqualTo("Method Not Allowed");
                    assertThat(errorResponse.getPath()).isEqualTo("/api/users");

                    // A mensagem da exceção pode conter informações específicas
                    assertThat(errorResponse.getMessage()).isEqualTo(ex.getMessage());

                    // Verifica timestamp
                    assertThat(errorResponse.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve lidar com MethodNotAllowedException com múltiplos métodos suportados")
    void handleMethodNotAllowed_WithMultipleSupportedMethods_ShouldReturnErrorResponse() {
        // Arrange
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn("/api/resource");
        when(request.getPath()).thenReturn(requestPath);
        when(request.getMethod()).thenReturn(HttpMethod.PATCH);

        Set<HttpMethod> supportedMethods = new HashSet<>();
        supportedMethods.add(HttpMethod.GET);
        supportedMethods.add(HttpMethod.POST);
        supportedMethods.add(HttpMethod.PUT);
        supportedMethods.add(HttpMethod.DELETE);

        MethodNotAllowedException ex = new MethodNotAllowedException(
                HttpMethod.PATCH,
                supportedMethods
        );

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleMethodNotAllowed(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    ErrorResponse errorResponse = responseEntity.getBody();

                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
                    assertThat(errorResponse).isNotNull();
                    assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
                    assertThat(errorResponse.getError()).isEqualTo("Method Not Allowed");
                    assertThat(errorResponse.getPath()).isEqualTo("/api/resource");
                    assertThat(errorResponse.getMessage()).isEqualTo(ex.getMessage());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve lidar com MethodNotAllowedException para caminho diferente")
    void handleMethodNotAllowed_DifferentPath_ShouldReturnCorrectPath() {
        // Arrange
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn("/api/v2/items/123");
        when(request.getPath()).thenReturn(requestPath);
        when(request.getMethod()).thenReturn(HttpMethod.TRACE);

        Set<HttpMethod> supportedMethods = new HashSet<>();
        supportedMethods.add(HttpMethod.GET);
        supportedMethods.add(HttpMethod.DELETE);

        MethodNotAllowedException ex = new MethodNotAllowedException(
                HttpMethod.TRACE,
                supportedMethods
        );

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleMethodNotAllowed(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    ErrorResponse errorResponse = responseEntity.getBody();

                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
                    assertThat(errorResponse).isNotNull();
                    assertThat(errorResponse.getPath()).isEqualTo("/api/v2/items/123");
                    // A mensagem deve conter informações sobre o método não permitido
                    assertThat(errorResponse.getMessage()).contains("TRACE");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Deve verificar estrutura completa do ErrorResponse")
    void handleMethodNotAllowed_ShouldReturnCompleteErrorResponseStructure() {
        // Arrange
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn("/api/test");
        when(request.getPath()).thenReturn(requestPath);
        when(request.getMethod()).thenReturn(HttpMethod.HEAD);

        Set<HttpMethod> supportedMethods = new HashSet<>();
        supportedMethods.add(HttpMethod.GET);

        MethodNotAllowedException ex = new MethodNotAllowedException(
                HttpMethod.HEAD,
                supportedMethods
        );

        // Act
        Mono<ResponseEntity<ErrorResponse>> result = exceptionHandler.handleMethodNotAllowed(ex, exchange);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    ErrorResponse errorResponse = responseEntity.getBody();

                    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
                    assertThat(errorResponse).isNotNull();

                    assertThat(errorResponse.getStatus()).isEqualTo(405);
                    assertThat(errorResponse.getError()).isEqualTo("Method Not Allowed");
                    assertThat(errorResponse.getPath()).isEqualTo("/api/test");
                    assertThat(errorResponse.getMessage()).isEqualTo(ex.getMessage());
                    assertThat(errorResponse.getTimestamp()).isNotNull();

                    assertThat(errorResponse.getType()).isNull();
                    assertThat(errorResponse.getInstance()).isNull();
                })
                .verifyComplete();
    }
}
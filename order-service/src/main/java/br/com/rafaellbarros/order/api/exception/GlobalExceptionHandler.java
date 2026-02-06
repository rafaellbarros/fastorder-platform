package br.com.rafaellbarros.order.api.exception;

import br.com.rafaellbarros.order.domain.exception.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(
            WebExchangeBindException ex,
            ServerWebExchange exchange
    ) {

        List<String> details = ex.getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Validation error at {} → {}", exchange.getRequest().getPath(), details);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Request validation failed")
                .path(exchange.getRequest().getPath().value())
                .details(details)
                .build();

        return Mono.just(ResponseEntity.badRequest().body(response));
    }


    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDomain(
            DomainException ex,
            ServerWebExchange exchange
    ) {
        log.warn("Business rule violation → {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Business Error")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.unprocessableEntity().body(response));
    }


    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(
            Throwable ex,
            ServerWebExchange exchange
    ) {
        log.error("Unexpected error", ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNoResource(
            NoResourceFoundException ex,
            ServerWebExchange exchange
    ) {
        // Ignorar favicon.ico Swagger WebFlux
        String path = exchange.getRequest().getPath().value();
        if (path.equals("/favicon.ico")) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        log.warn("Resource not found: {} {}",
                exchange.getRequest().getMethod(),
                path);

        String httpMethod = exchange.getRequest().getMethod().name();
        String message = String.format("No handler found for %s %s", httpMethod, path);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(message)
                .path(path)
                .type("about:blank")
                .instance(path)
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleMethodNotAllowed(
            MethodNotAllowedException ex,
            ServerWebExchange exchange
    ) {
        log.warn("Method not allowed: {} {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message(ex.getMessage())
                .path(exchange.getRequest().getPath().value())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response));
    }
}
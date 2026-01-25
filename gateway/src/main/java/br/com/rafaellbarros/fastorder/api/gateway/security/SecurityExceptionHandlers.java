package br.com.rafaellbarros.fastorder.api.gateway.security;

import br.com.rafaellbarros.fastorder.api.gateway.dto.response.ApiErrorResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityExceptionHandlers {

    private final ObjectMapper objectMapper;

    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> {

            log.warn("401 Unauthorized: {}", ex.getMessage());

            return writeErrorResponse(
                    exchange,
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED",
                    "Token ausente, inválido ou expirado"
            );
        };
    }

    public ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, ex) -> {

            log.warn("403 Forbidden: {}", ex.getMessage());

            return writeErrorResponse(
                    exchange,
                    HttpStatus.FORBIDDEN,
                    "FORBIDDEN",
                    "Você não possui permissão para acessar este recurso"
            );
        };
    }

    private Mono<Void> writeErrorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            String error,
            String message) {

        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        var body = ApiErrorResponseDTO.builder()
                .error(error)
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .timestamp(Instant.now())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            var buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar resposta de erro", e);
            return response.setComplete();
        }
    }
}

package br.com.rafaellbarros.fastorder.api.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@Slf4j
public class SecurityExceptionHandlers {

    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> {

            log.warn("401 Unauthorized: {}", ex.getMessage());

            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            var body = """
                    {
                      "error": "UNAUTHORIZED",
                      "message": "Token ausente, inválido ou expirado",
                      "path": "%s",
                      "timestamp": "%s"
                    }
                    """.formatted(
                        exchange.getRequest().getPath(),
                        Instant.now()
                    );

            var buffer = response.bufferFactory()
                    .wrap(body.getBytes(StandardCharsets.UTF_8));

            return response.writeWith(Mono.just(buffer));
        };
    }

    public ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, ex) -> {

            log.warn("403 Forbidden: {}", ex.getMessage());

            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            var body = """
                    {
                      "error": "FORBIDDEN",
                      "message": "Você não possui permissão para acessar este recurso",
                      "path": "%s",
                      "timestamp": "%s"
                    }
                    """.formatted(
                        exchange.getRequest().getPath(),
                        Instant.now()
                    );

            var buffer = response.bufferFactory()
                    .wrap(body.getBytes(StandardCharsets.UTF_8));

            return response.writeWith(Mono.just(buffer));
        };
    }
}

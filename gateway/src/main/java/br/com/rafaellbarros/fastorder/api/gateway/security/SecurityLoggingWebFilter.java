package br.com.rafaellbarros.fastorder.api.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityLoggingWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        long start = System.currentTimeMillis();

        return chain.filter(exchange)
                .doOnError(error -> log.error("Security error: {}", error.getMessage()))
                .doFinally(signal -> {
                    var status = exchange.getResponse().getStatusCode();
                    long duration = System.currentTimeMillis() - start;

                    log.info("SECURITY {} {} -> {} ({} ms)",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI(),
                            status,
                            duration);
                });
    }
}
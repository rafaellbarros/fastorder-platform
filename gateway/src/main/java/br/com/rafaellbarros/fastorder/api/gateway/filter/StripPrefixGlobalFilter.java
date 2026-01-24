package br.com.rafaellbarros.fastorder.api.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StripPrefixGlobalFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getRawPath();

        // Remove o primeiro segmento (nome do serviço)
        String newPath = path.replaceFirst("/[^/]+", "");

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .path(newPath.isEmpty() ? "/" : newPath)
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }
}

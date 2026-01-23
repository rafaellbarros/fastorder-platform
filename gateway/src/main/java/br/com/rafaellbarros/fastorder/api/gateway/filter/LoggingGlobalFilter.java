package br.com.rafaellbarros.fastorder.api.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long start = System.currentTimeMillis();

        return chain.filter(exchange)
                .doFinally(signal -> {
                    var status = exchange.getResponse().getStatusCode();
                    long duration = System.currentTimeMillis() - start;

                    log.info("GATEWAY {} {} -> {} ({} ms)",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI(),
                            status,
                            duration);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}




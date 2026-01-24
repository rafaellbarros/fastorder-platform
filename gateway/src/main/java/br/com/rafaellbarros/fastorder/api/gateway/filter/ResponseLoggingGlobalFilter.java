package br.com.rafaellbarros.fastorder.api.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class ResponseLoggingGlobalFilter implements GlobalFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = Flux.from(body);

                    return super.writeWith(
                            fluxBody.map(dataBuffer -> {

                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);

                                DataBufferUtils.release(dataBuffer);

                                String responseBody = new String(content, StandardCharsets.UTF_8);

                                log.info("""
                                
                                🌐 GATEWAY RESPONSE
                                Route: {}
                                Method: {}
                                Path: {}
                                Status: {}
                                Response Body: {}
                                """,
                                        exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR),
                                        exchange.getRequest().getMethod(),
                                        exchange.getRequest().getURI(),
                                        getStatusCode(),
                                        responseBody
                                );

                                return bufferFactory.wrap(content);
                            })
                    );
                }

                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }
}

package br.com.rafaellbarros.order.application.command.service;

import br.com.rafaellbarros.order.application.command.dto.CreateOrderRequest;
import br.com.rafaellbarros.order.application.command.handler.CreateOrderHandler;
import br.com.rafaellbarros.order.application.command.mapper.OrderCommandMapper;
import br.com.rafaellbarros.order.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final CreateOrderHandler handler;
    private final OrderCommandMapper mapper;

    public Mono<String> createOrder(CreateOrderRequest request) {
        return handler.handle(mapper.toCommand(request))
                .onErrorMap(DomainException.class, ex -> ex)
                .onErrorMap(ex -> {
                    if (ex instanceof DomainException) {
                        return ex;
                    }
                    log.error("Error creating order", ex);
                    return new RuntimeException("Failed to create order", ex);
                });
    }
}
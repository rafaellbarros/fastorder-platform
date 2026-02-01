package br.com.rafaellbarros.order.application.command.service;

import br.com.rafaellbarros.order.application.command.dto.CreateOrderRequest;
import br.com.rafaellbarros.order.application.command.handler.CreateOrderHandler;
import br.com.rafaellbarros.order.application.command.mapper.OrderCommandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final CreateOrderHandler handler;
    private final OrderCommandMapper mapper;

    public Mono<String> createOrder(CreateOrderRequest request) {
        return handler.handle(mapper.toCommand(request));
    }
}
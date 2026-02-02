package br.com.rafaellbarros.order.api.query;

import br.com.rafaellbarros.order.application.query.service.OrderQueryService;
import br.com.rafaellbarros.order.infrastructure.persistence.readmodel.OrderView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Queries", description = "Consultas de pedidos")
public class OrderQueryController {

    private final OrderQueryService service;

    @Operation(summary = "Buscar pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Não encontrado")
    })
    @GetMapping("/{id}")
    public Mono<OrderView> getById(
            @Parameter(description = "ID do pedido")
            @PathVariable String id
    ) {
        return service.findById(id);
    }

    @Operation(summary = "Listar pedidos do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/user/{userId}")
    public Flux<OrderView> listByUser(
            @Parameter(description = "ID do usuário")
            @PathVariable String userId
    ) {
        return service.findByUser(userId);
    }
}
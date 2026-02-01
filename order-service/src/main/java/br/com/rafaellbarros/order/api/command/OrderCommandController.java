package br.com.rafaellbarros.order.api.command;

import br.com.rafaellbarros.order.application.command.dto.CreateOrderRequest;
import br.com.rafaellbarros.order.application.command.service.OrderCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Commands", description = "Endpoints de escrita para pedidos")
public class OrderCommandController {

    private final OrderCommandService service;

    @Operation(
            summary = "Criar pedido",
            description = "Cria um novo pedido no sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido criado com sucesso",
                    content = @Content(schema = @Schema(type = "string"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autorizado"
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada"
            )
    })
    @PostMapping
    public Mono<ResponseEntity<String>> create(@Valid @RequestBody CreateOrderRequest request) {
        return service.createOrder(request)
                .map(ResponseEntity::ok);
    }
}

package br.com.rafaellbarros.order.api.command;

import br.com.rafaellbarros.order.api.exception.ErrorResponse;
import br.com.rafaellbarros.order.application.command.dto.CreateOrderRequest;
import br.com.rafaellbarros.order.application.command.service.OrderCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Commands", description = "Endpoints de escrita para pedidos")
public class OrderCommandController {

    private final OrderCommandService service;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateOrderResponse {
        private String orderId;
        private String status;
        private Instant createdAt;
    }

    @Operation(
            summary = "Criar pedido",
            description = "Cria um novo pedido no sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido criado com sucesso",
                    content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autorizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<CreateOrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        return service.createOrder(request)
                .map(orderId -> CreateOrderResponse.builder()
                        .orderId(orderId)
                        .status("CREATED")
                        .createdAt(Instant.now())
                        .build())
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
}

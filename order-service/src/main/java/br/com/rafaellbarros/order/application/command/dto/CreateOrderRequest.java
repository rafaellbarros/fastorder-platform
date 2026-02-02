package br.com.rafaellbarros.order.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@Schema(
        description = "Request para criação de um novo pedido",
        requiredProperties = {"userId", "items"}
)
public class CreateOrderRequest {

    @NotBlank
    @Schema(description = "ID do usuário", example = "user-123")
    private String userId;

    @NotEmpty
    @Schema(description = "Itens do pedido")
    private List<OrderItemRequest> items;
}

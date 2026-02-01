package br.com.rafaellbarros.order.application.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Item individual de um pedido")
public class OrderItemRequest {

    @NotBlank
    @Schema(description = "ID do produto", example = "prod-001")
    private String productId;

    @Positive
    @Schema(description = "Quantidade", example = "2")
    private Integer quantity;

    @Positive
    @Schema(description = "Preço unitário", example = "149.99")
    private BigDecimal price;

}
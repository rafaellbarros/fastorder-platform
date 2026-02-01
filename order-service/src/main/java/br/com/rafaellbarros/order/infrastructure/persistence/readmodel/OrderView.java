package br.com.rafaellbarros.order.infrastructure.persistence.readmodel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders_view")
public class OrderView {

    @Id
    @Schema(description = "ID do pedido")
    private String orderId;

    @Schema(description = "ID do usuário")
    private String userId;

    @Schema(description = "Status do pedido")
    private String status;

    @Schema(description = "Valor total")
    private BigDecimal totalAmount;

    @Schema(description = "Itens do pedido")
    private List<ItemView> items;

    @Schema(description = "Data de criação")
    private Instant createdAt;

    @Schema(description = "Data de atualização")
    private Instant updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemView {

        @Schema(description = "ID do produto")
        private String productId;

        @Schema(description = "Quantidade")
        private Integer quantity;

        @Schema(description = "Preço unitário")
        private BigDecimal price;
    }
}
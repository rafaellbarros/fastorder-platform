package br.com.rafaellbarros.order.infrastructure.persistence.readmodel;

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
    private String orderId;

    private String userId;

    private String status;

    private List<ItemView> items;

    private BigDecimal totalAmount;

    private Instant createdAt;

    private Instant updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemView {
        private String productId;
        private Integer quantity;
        private BigDecimal price;
    }
}
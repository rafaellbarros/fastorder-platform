package br.com.rafaellbarros.order.application.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {

    @NotBlank
    private String productId;

    @Positive
    private Integer quantity;

    @Positive
    private BigDecimal price;

}
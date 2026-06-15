package com.tomato.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {

    @NotNull
    private Long menuItemId;

    @NotNull
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;
}

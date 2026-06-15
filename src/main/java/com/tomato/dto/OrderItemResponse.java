package com.tomato.dto;

import com.tomato.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderItemResponse {

    private final Long menuItemId;
    private final String name;
    private final Integer quantity;
    private final BigDecimal itemPrice;
    private final BigDecimal totalPrice;

    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getMenuItem().getId(),
                orderItem.getMenuItem().getName(),
                orderItem.getQuantity(),
                orderItem.getItemPrice(),
                orderItem.getTotalPrice()
        );
    }
}

package com.tomato.dto;

import com.tomato.model.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AvailableOrderResponse {

    private final Long orderId;
    private final String restaurantName;
    private final String restaurantAddress;
    private final String deliveryAddress;
    private final BigDecimal totalAmount;

    public static AvailableOrderResponse from(Order order) {
        return new AvailableOrderResponse(
                order.getId(),
                order.getRestaurant().getName(),
                order.getRestaurant().getAddress(),
                order.getDeliveryAddress(),
                order.getTotalAmount()
        );
    }
}

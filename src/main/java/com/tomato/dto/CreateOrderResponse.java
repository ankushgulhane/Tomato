package com.tomato.dto;

import com.tomato.model.Order;
import com.tomato.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreateOrderResponse {

    private final Long orderId;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;

    public static CreateOrderResponse from(Order order) {
        return new CreateOrderResponse(order.getId(), order.getStatus(), order.getTotalAmount(), order.getCreatedAt());
    }
}

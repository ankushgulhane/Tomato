package com.tomato.dto;

import com.tomato.model.Order;
import com.tomato.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class OrderResponse {

    private final Long id;
    private final Long customerId;
    private final Long restaurantId;
    private final String restaurantName;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final String deliveryAddress;
    private final String specialInstructions;
    private final List<OrderItemResponse> items;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getRestaurant().getId(),
                order.getRestaurant().getName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getSpecialInstructions(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}

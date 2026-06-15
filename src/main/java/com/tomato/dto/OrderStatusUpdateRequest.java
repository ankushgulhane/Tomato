package com.tomato.dto;

import com.tomato.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusUpdateRequest {

    @NotNull
    private OrderStatus status;
}

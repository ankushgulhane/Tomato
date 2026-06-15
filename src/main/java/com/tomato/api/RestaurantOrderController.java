package com.tomato.api;

import com.tomato.dto.OrderResponse;
import com.tomato.dto.OrderStatusUpdateRequest;
import com.tomato.model.OrderStatus;
import com.tomato.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESTAURANT')")
public class RestaurantOrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> getRestaurantOrders(@PathVariable Long restaurantId,
                                                     @RequestParam(required = false) OrderStatus status) {
        return orderService.getRestaurantOrders(restaurantId, status);
    }

    @PutMapping("/{orderId}/accept")
    public OrderResponse acceptOrder(@PathVariable Long restaurantId, @PathVariable Long orderId) {
        return orderService.acceptOrder(restaurantId, orderId);
    }

    @PutMapping("/{orderId}/reject")
    public OrderResponse rejectOrder(@PathVariable Long restaurantId, @PathVariable Long orderId) {
        return orderService.rejectOrder(restaurantId, orderId);
    }

    @PutMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable Long restaurantId, @PathVariable Long orderId,
                                        @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updatePreparationStatus(restaurantId, orderId, request);
    }
}

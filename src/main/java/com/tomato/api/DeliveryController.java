package com.tomato.api;

import com.tomato.dto.AvailableOrderResponse;
import com.tomato.dto.OrderResponse;
import com.tomato.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DELIVERY_PARTNER')")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/orders/available")
    public List<AvailableOrderResponse> getAvailableOrders() {
        return deliveryService.getAvailableOrders();
    }

    @PutMapping("/orders/{orderId}/accept")
    public OrderResponse acceptOrder(@PathVariable Long orderId) {
        return deliveryService.acceptOrder(orderId);
    }

    @PutMapping("/orders/{orderId}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineOrder(@PathVariable Long orderId) {
        deliveryService.declineOrder(orderId);
    }

    @PutMapping("/orders/{orderId}/pickup")
    public OrderResponse confirmPickup(@PathVariable Long orderId) {
        return deliveryService.confirmPickup(orderId);
    }

    @PutMapping("/orders/{orderId}/deliver")
    public OrderResponse confirmDelivery(@PathVariable Long orderId) {
        return deliveryService.confirmDelivery(orderId);
    }
}

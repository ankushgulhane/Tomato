package com.tomato.api;

import com.tomato.dto.PaymentRequest;
import com.tomato.dto.PaymentResponse;
import com.tomato.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    @PostMapping("/mock")
    @PreAuthorize("hasRole('USER')")
    public PaymentResponse processPayment(@Valid @RequestBody PaymentRequest request) {
        return orderService.pay(request.getOrderId(), request);
    }
}

package com.tomato.dto;

import com.tomato.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResponse {

    private final PaymentStatus status;
    private final String transactionId;
}

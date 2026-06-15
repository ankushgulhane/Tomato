package com.tomato.integration;

import java.math.BigDecimal;

/**
 * Strategy interface for payment provider integrations. {@link MockPaymentProvider}
 * is the V1 implementation; real gateways can be added later without changing
 * {@code PaymentService}.
 */
public interface PaymentProvider {

    PaymentResult charge(BigDecimal amount, String paymentMethod);

    record PaymentResult(boolean success, String providerReference) {
    }
}

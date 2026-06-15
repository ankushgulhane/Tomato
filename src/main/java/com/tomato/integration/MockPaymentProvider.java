package com.tomato.integration;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * V1 mock payment provider. Always succeeds for valid (positive) amounts after
 * simulating a short processing delay, as described in the low-level design (Section 13).
 */
@Component
public class MockPaymentProvider implements PaymentProvider {

    private static final long SIMULATED_DELAY_MS = 100;

    @Override
    public PaymentResult charge(BigDecimal amount, String paymentMethod) {
        simulateProcessingDelay();

        if (amount == null || amount.signum() <= 0) {
            return new PaymentResult(false, null);
        }

        return new PaymentResult(true, UUID.randomUUID().toString());
    }

    private void simulateProcessingDelay() {
        try {
            Thread.sleep(SIMULATED_DELAY_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}

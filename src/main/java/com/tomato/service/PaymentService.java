package com.tomato.service;

import com.tomato.integration.PaymentProvider;
import com.tomato.model.Order;
import com.tomato.model.PaymentStatus;
import com.tomato.model.PaymentTransaction;
import com.tomato.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentProvider paymentProvider;

    public PaymentTransaction processPayment(Order order, BigDecimal amount, String paymentMethod) {
        PaymentProvider.PaymentResult result = paymentProvider.charge(amount, paymentMethod);

        PaymentTransaction transaction = PaymentTransaction.builder()
                .order(order)
                .amount(amount)
                .status(result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .providerReference(result.providerReference())
                .processedAt(LocalDateTime.now())
                .build();

        return paymentTransactionRepository.save(transaction);
    }

    public void refundIfPaid(Order order) {
        paymentTransactionRepository.findFirstByOrderIdOrderByProcessedAtDesc(order.getId())
                .filter(transaction -> transaction.getStatus() == PaymentStatus.SUCCESS)
                .ifPresent(transaction -> {
                    transaction.setStatus(PaymentStatus.REFUNDED);
                    paymentTransactionRepository.save(transaction);
                });
    }
}

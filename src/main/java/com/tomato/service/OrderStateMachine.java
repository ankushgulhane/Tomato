package com.tomato.service;

import com.tomato.exception.InvalidOrderStateException;
import com.tomato.model.Order;
import com.tomato.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the {@link OrderStatus} transition table from the design document (Section 10).
 */
@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = buildTransitions();

    private static final Set<OrderStatus> CANCELLABLE_STATUSES = EnumSet.of(
            OrderStatus.CREATED,
            OrderStatus.PLACED,
            OrderStatus.RESTAURANT_CONFIRMED,
            OrderStatus.PREPARING
    );

    private static Map<OrderStatus, Set<OrderStatus>> buildTransitions() {
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(OrderStatus.CREATED, EnumSet.of(OrderStatus.PLACED, OrderStatus.PAYMENT_FAILED, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.PAYMENT_FAILED, EnumSet.of(OrderStatus.CREATED));
        transitions.put(OrderStatus.PLACED, EnumSet.of(OrderStatus.RESTAURANT_CONFIRMED, OrderStatus.RESTAURANT_REJECTED, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.RESTAURANT_CONFIRMED, EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.PREPARING, EnumSet.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED));
        transitions.put(OrderStatus.READY_FOR_PICKUP, EnumSet.of(OrderStatus.OUT_FOR_DELIVERY));
        transitions.put(OrderStatus.OUT_FOR_DELIVERY, EnumSet.of(OrderStatus.DELIVERED));
        transitions.put(OrderStatus.RESTAURANT_REJECTED, EnumSet.noneOf(OrderStatus.class));
        transitions.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        transitions.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
        return Map.copyOf(transitions);
    }

    public boolean canTransition(OrderStatus from, OrderStatus to) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public void transition(Order order, OrderStatus to) {
        OrderStatus from = order.getStatus();
        if (!canTransition(from, to)) {
            throw new InvalidOrderStateException(
                    "Cannot transition order " + order.getId() + " from " + from + " to " + to);
        }
        order.setStatus(to);
    }

    public boolean isCancellable(OrderStatus status) {
        return CANCELLABLE_STATUSES.contains(status);
    }
}

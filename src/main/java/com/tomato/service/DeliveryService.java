package com.tomato.service;

import com.tomato.dto.AvailableOrderResponse;
import com.tomato.dto.OrderResponse;
import com.tomato.exception.InvalidOrderStateException;
import com.tomato.exception.ResourceNotFoundException;
import com.tomato.model.DeliveryAssignment;
import com.tomato.model.DeliveryStatus;
import com.tomato.model.NotificationType;
import com.tomato.model.Order;
import com.tomato.model.OrderStatus;
import com.tomato.model.User;
import com.tomato.repository.DeliveryAssignmentRepository;
import com.tomato.repository.OrderRepository;
import com.tomato.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final OrderRepository orderRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final OrderStateMachine orderStateMachine;
    private final NotificationService notificationService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<AvailableOrderResponse> getAvailableOrders() {
        return orderRepository.findByStatus(OrderStatus.READY_FOR_PICKUP).stream()
                .filter(order -> !deliveryAssignmentRepository.existsByOrderIdAndStatus(order.getId(), DeliveryStatus.ACCEPTED))
                .map(AvailableOrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse acceptOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new InvalidOrderStateException("Order " + orderId + " is not ready for pickup");
        }
        if (deliveryAssignmentRepository.existsByOrderIdAndStatus(orderId, DeliveryStatus.ACCEPTED)) {
            throw new InvalidOrderStateException("Order " + orderId + " has already been accepted by another delivery partner");
        }

        User partner = userService.getUserEntityOrThrow(SecurityUtils.getCurrentUserId());

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .order(order)
                .deliveryPartner(partner)
                .status(DeliveryStatus.ACCEPTED)
                .acceptedAt(LocalDateTime.now())
                .build();
        deliveryAssignmentRepository.save(assignment);

        notificationService.notify(order.getCustomer(), order, NotificationType.ORDER_UPDATED,
                "A delivery partner has been assigned to your order #" + order.getId() + ".");
        notificationService.notify(order.getRestaurant().getOwner(), order, NotificationType.ORDER_UPDATED,
                "A delivery partner accepted order #" + order.getId() + ".");

        return OrderResponse.from(order);
    }

    @Transactional
    public void declineOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        User partner = userService.getUserEntityOrThrow(SecurityUtils.getCurrentUserId());

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .order(order)
                .deliveryPartner(partner)
                .status(DeliveryStatus.DECLINED)
                .declinedAt(LocalDateTime.now())
                .build();
        deliveryAssignmentRepository.save(assignment);
    }

    @Transactional
    public OrderResponse confirmPickup(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        DeliveryAssignment assignment = getAssignmentForCurrentPartner(order, DeliveryStatus.ACCEPTED);

        orderStateMachine.transition(order, OrderStatus.OUT_FOR_DELIVERY);
        assignment.setStatus(DeliveryStatus.PICKED_UP);
        assignment.setPickedUpAt(LocalDateTime.now());

        deliveryAssignmentRepository.save(assignment);
        orderRepository.save(order);

        notificationService.notify(order.getCustomer(), order, NotificationType.ORDER_PICKED_UP,
                "Your order #" + order.getId() + " has been picked up and is on the way.");

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse confirmDelivery(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        DeliveryAssignment assignment = getAssignmentForCurrentPartner(order, DeliveryStatus.PICKED_UP);

        orderStateMachine.transition(order, OrderStatus.DELIVERED);
        assignment.setStatus(DeliveryStatus.DELIVERED);
        assignment.setDeliveredAt(LocalDateTime.now());

        deliveryAssignmentRepository.save(assignment);
        orderRepository.save(order);

        notificationService.notify(order.getCustomer(), order, NotificationType.ORDER_DELIVERED,
                "Your order #" + order.getId() + " has been delivered. Enjoy your meal!");

        return OrderResponse.from(order);
    }

    private DeliveryAssignment getAssignmentForCurrentPartner(Order order, DeliveryStatus expectedStatus) {
        Long partnerId = SecurityUtils.getCurrentUserId();
        DeliveryAssignment assignment = deliveryAssignmentRepository.findByOrderIdAndDeliveryPartnerId(order.getId(), partnerId)
                .orElseThrow(() -> new AccessDeniedException("You are not assigned to order " + order.getId()));

        if (assignment.getStatus() != expectedStatus) {
            throw new InvalidOrderStateException(
                    "Delivery assignment for order " + order.getId() + " is not in status " + expectedStatus);
        }

        return assignment;
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }
}

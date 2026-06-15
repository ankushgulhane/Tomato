package com.tomato.service;

import com.tomato.dto.CreateOrderRequest;
import com.tomato.dto.CreateOrderResponse;
import com.tomato.dto.OrderItemRequest;
import com.tomato.dto.OrderResponse;
import com.tomato.dto.OrderStatusUpdateRequest;
import com.tomato.dto.PaymentRequest;
import com.tomato.dto.PaymentResponse;
import com.tomato.exception.BadRequestException;
import com.tomato.exception.InvalidOrderStateException;
import com.tomato.exception.ResourceNotFoundException;
import com.tomato.model.MenuItem;
import com.tomato.model.NotificationType;
import com.tomato.model.Order;
import com.tomato.model.OrderItem;
import com.tomato.model.OrderStatus;
import com.tomato.model.PaymentStatus;
import com.tomato.model.PaymentTransaction;
import com.tomato.model.Restaurant;
import com.tomato.model.RestaurantStatus;
import com.tomato.model.Role;
import com.tomato.model.User;
import com.tomato.repository.MenuItemRepository;
import com.tomato.repository.OrderRepository;
import com.tomato.security.SecurityUtils;
import com.tomato.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final OrderStateMachine orderStateMachine;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        SecurityUtils.requireSelfOrAdmin(request.getUserId());

        User customer = userService.getUserEntityOrThrow(request.getUserId());
        Restaurant restaurant = restaurantService.getRestaurantEntityOrThrow(request.getRestaurantId());

        if (restaurant.getStatus() != RestaurantStatus.OPEN) {
            throw new BadRequestException("Restaurant " + restaurant.getId() + " is currently closed");
        }

        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> buildOrderItem(restaurant, itemRequest))
                .collect(Collectors.toList());

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customer(customer)
                .restaurant(restaurant)
                .totalAmount(totalAmount)
                .status(OrderStatus.CREATED)
                .deliveryAddress(request.getDeliveryAddress())
                .specialInstructions(request.getSpecialInstructions())
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setOrderItems(orderItems);

        return CreateOrderResponse.from(orderRepository.save(order));
    }

    private OrderItem buildOrderItem(Restaurant restaurant, OrderItemRequest itemRequest) {
        MenuItem menuItem = menuItemRepository.findByIdAndRestaurantId(itemRequest.getMenuItemId(), restaurant.getId())
                .orElseThrow(() -> new BadRequestException(
                        "Menu item " + itemRequest.getMenuItemId() + " does not belong to restaurant " + restaurant.getId()));

        if (!Boolean.TRUE.equals(menuItem.getActive())) {
            throw new BadRequestException("Menu item " + menuItem.getId() + " is not currently available");
        }

        BigDecimal totalPrice = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

        return OrderItem.builder()
                .menuItem(menuItem)
                .quantity(itemRequest.getQuantity())
                .itemPrice(menuItem.getPrice())
                .totalPrice(totalPrice)
                .build();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = getOrderEntityOrThrow(orderId);
        assertCanView(order);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        SecurityUtils.requireSelfOrAdmin(userId);
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse pay(Long orderId, PaymentRequest request) {
        Order order = getOrderEntityOrThrow(orderId);

        if (!order.getCustomer().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Only the customer who placed the order can pay for it");
        }
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAYMENT_FAILED) {
            throw new InvalidOrderStateException(
                    "Order " + orderId + " is not awaiting payment (status: " + order.getStatus() + ")");
        }
        if (order.getTotalAmount().compareTo(request.getAmount()) != 0) {
            throw new BadRequestException("Payment amount does not match order total");
        }

        if (order.getStatus() == OrderStatus.PAYMENT_FAILED) {
            orderStateMachine.transition(order, OrderStatus.CREATED);
        }

        PaymentTransaction transaction = paymentService.processPayment(order, request.getAmount(), request.getPaymentMethod());

        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            orderStateMachine.transition(order, OrderStatus.PLACED);
            notificationService.notify(order.getCustomer(), order, NotificationType.ORDER_PLACED,
                    "Your order #" + order.getId() + " has been placed.");
            notificationService.notify(order.getRestaurant().getOwner(), order, NotificationType.ORDER_PLACED,
                    "New order #" + order.getId() + " received.");
        } else {
            orderStateMachine.transition(order, OrderStatus.PAYMENT_FAILED);
        }

        orderRepository.save(order);

        return new PaymentResponse(transaction.getStatus(), transaction.getProviderReference());
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = getOrderEntityOrThrow(orderId);
        UserPrincipal current = SecurityUtils.getCurrentUser();

        boolean allowed = order.getCustomer().getId().equals(current.getId()) || current.getRole() == Role.ADMIN;
        if (!allowed) {
            throw new AccessDeniedException("Only the customer or an admin can cancel this order");
        }

        if (!orderStateMachine.isCancellable(order.getStatus())) {
            throw new InvalidOrderStateException(
                    "Order " + orderId + " cannot be cancelled from status " + order.getStatus());
        }

        orderStateMachine.transition(order, OrderStatus.CANCELLED);
        paymentService.refundIfPaid(order);

        notificationService.notify(order.getCustomer(), order, NotificationType.ORDER_CANCELLED,
                "Order #" + order.getId() + " was cancelled.");
        notificationService.notify(order.getRestaurant().getOwner(), order, NotificationType.ORDER_CANCELLED,
                "Order #" + order.getId() + " was cancelled by the customer.");

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(Long restaurantId, OrderStatus statusFilter) {
        restaurantService.getOwnedRestaurantOrThrow(restaurantId);

        List<Order> orders = statusFilter != null
                ? orderRepository.findByRestaurantIdAndStatusOrderByCreatedAtDesc(restaurantId, statusFilter)
                : orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);

        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse acceptOrder(Long restaurantId, Long orderId) {
        Order order = getRestaurantOrderOrThrow(restaurantId, orderId);
        orderStateMachine.transition(order, OrderStatus.RESTAURANT_CONFIRMED);

        notificationService.notify(order.getCustomer(), order, NotificationType.ORDER_CONFIRMED,
                "Your order #" + order.getId() + " was accepted by the restaurant.");

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse rejectOrder(Long restaurantId, Long orderId) {
        Order order = getRestaurantOrderOrThrow(restaurantId, orderId);
        orderStateMachine.transition(order, OrderStatus.RESTAURANT_REJECTED);
        paymentService.refundIfPaid(order);

        notificationService.notify(order.getCustomer(), order, NotificationType.ORDER_REJECTED,
                "Your order #" + order.getId() + " was rejected by the restaurant.");

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updatePreparationStatus(Long restaurantId, Long orderId, OrderStatusUpdateRequest request) {
        Order order = getRestaurantOrderOrThrow(restaurantId, orderId);
        OrderStatus target = request.getStatus();

        if (target != OrderStatus.PREPARING && target != OrderStatus.READY_FOR_PICKUP) {
            throw new BadRequestException("Restaurants can only update an order to PREPARING or READY_FOR_PICKUP");
        }

        orderStateMachine.transition(order, target);

        notificationService.notify(order.getCustomer(), order, NotificationType.ORDER_UPDATED,
                "Your order #" + order.getId() + " is now " + target + ".");

        return OrderResponse.from(orderRepository.save(order));
    }

    private Order getRestaurantOrderOrThrow(Long restaurantId, Long orderId) {
        restaurantService.getOwnedRestaurantOrThrow(restaurantId);
        Order order = getOrderEntityOrThrow(orderId);

        if (!order.getRestaurant().getId().equals(restaurantId)) {
            throw new ResourceNotFoundException("Order " + orderId + " does not belong to restaurant " + restaurantId);
        }

        return order;
    }

    Order getOrderEntityOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private void assertCanView(Order order) {
        UserPrincipal current = SecurityUtils.getCurrentUser();
        boolean allowed = current.getRole() == Role.ADMIN
                || current.getRole() == Role.DELIVERY_PARTNER
                || order.getCustomer().getId().equals(current.getId())
                || order.getRestaurant().getOwner().getId().equals(current.getId());

        if (!allowed) {
            throw new AccessDeniedException("Not allowed to view order " + order.getId());
        }
    }
}

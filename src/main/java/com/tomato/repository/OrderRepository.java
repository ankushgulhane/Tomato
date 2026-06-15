package com.tomato.repository;

import com.tomato.model.Order;
import com.tomato.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<Order> findByRestaurantIdAndStatusOrderByCreatedAtDesc(Long restaurantId, OrderStatus status);

    List<Order> findByStatus(OrderStatus status);
}

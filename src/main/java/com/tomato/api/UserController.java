package com.tomato.api;

import com.tomato.dto.NotificationResponse;
import com.tomato.dto.OrderResponse;
import com.tomato.dto.UserResponse;
import com.tomato.service.NotificationService;
import com.tomato.service.OrderService;
import com.tomato.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrderService orderService;
    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @GetMapping("/{userId}/orders")
    public List<OrderResponse> getUserOrders(@PathVariable Long userId) {
        return orderService.getUserOrders(userId);
    }

    @GetMapping("/{userId}/notifications")
    public List<NotificationResponse> getNotifications(@PathVariable Long userId) {
        return notificationService.getNotifications(userId);
    }
}

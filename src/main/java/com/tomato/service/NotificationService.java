package com.tomato.service;

import com.tomato.dto.NotificationResponse;
import com.tomato.exception.ResourceNotFoundException;
import com.tomato.model.Notification;
import com.tomato.model.NotificationType;
import com.tomato.model.Order;
import com.tomato.model.User;
import com.tomato.repository.NotificationRepository;
import com.tomato.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void notify(User user, Order order, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .order(order)
                .type(type)
                .message(message)
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getNotifications(Long userId) {
        SecurityUtils.requireSelfOrAdmin(userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        SecurityUtils.requireSelfOrAdmin(notification.getUser().getId());
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}

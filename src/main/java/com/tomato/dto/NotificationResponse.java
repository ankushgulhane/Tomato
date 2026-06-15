package com.tomato.dto;

import com.tomato.model.Notification;
import com.tomato.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {

    private final Long id;
    private final Long orderId;
    private final NotificationType type;
    private final String message;
    private final LocalDateTime createdAt;
    private final Boolean read;

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getOrder().getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getRead()
        );
    }
}

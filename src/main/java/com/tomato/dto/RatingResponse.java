package com.tomato.dto;

import com.tomato.model.Rating;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RatingResponse {

    private final Long id;
    private final Long orderId;
    private final Long restaurantId;
    private final Long userId;
    private final Integer score;
    private final String comment;
    private final LocalDateTime createdAt;

    public static RatingResponse from(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getOrder().getId(),
                rating.getRestaurant().getId(),
                rating.getUser().getId(),
                rating.getScore(),
                rating.getComment(),
                rating.getCreatedAt()
        );
    }
}

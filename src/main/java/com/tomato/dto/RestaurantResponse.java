package com.tomato.dto;

import com.tomato.model.Restaurant;
import com.tomato.model.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class RestaurantResponse {

    private final Long id;
    private final Long ownerId;
    private final String name;
    private final String cuisine;
    private final String address;
    private final String phone;
    private final BigDecimal averageRating;
    private final Integer ratingCount;
    private final RestaurantStatus status;

    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getOwner().getId(),
                restaurant.getName(),
                restaurant.getCuisine(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getAverageRating(),
                restaurant.getRatingCount(),
                restaurant.getStatus()
        );
    }
}

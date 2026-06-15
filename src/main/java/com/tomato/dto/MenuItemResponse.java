package com.tomato.dto;

import com.tomato.model.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MenuItemResponse {

    private final Long id;
    private final Long restaurantId;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final String category;
    private final Boolean active;

    public static MenuItemResponse from(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId(),
                menuItem.getRestaurant().getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.getActive()
        );
    }
}

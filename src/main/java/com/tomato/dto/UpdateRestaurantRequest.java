package com.tomato.dto;

import com.tomato.model.RestaurantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRestaurantRequest {

    @NotBlank
    private String name;

    private String cuisine;

    private String address;

    private String phone;

    @NotNull
    private RestaurantStatus status;
}

package com.tomato.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRestaurantRequest {

    @NotBlank
    private String name;

    private String cuisine;

    private String address;

    private String phone;
}

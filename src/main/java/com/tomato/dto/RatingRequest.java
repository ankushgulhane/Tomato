package com.tomato.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingRequest {

    @NotNull
    private Long orderId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;

    private String comment;
}

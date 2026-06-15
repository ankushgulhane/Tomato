package com.tomato.api;

import com.tomato.dto.CreateRestaurantRequest;
import com.tomato.dto.RatingRequest;
import com.tomato.dto.RatingResponse;
import com.tomato.dto.RestaurantResponse;
import com.tomato.dto.UpdateRestaurantRequest;
import com.tomato.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public List<RestaurantResponse> listRestaurants() {
        return restaurantService.listRestaurants();
    }

    @GetMapping("/search")
    public List<RestaurantResponse> searchRestaurants(@RequestParam String query) {
        return restaurantService.searchRestaurants(query);
    }

    @GetMapping("/{restaurantId}")
    public RestaurantResponse getRestaurant(@PathVariable Long restaurantId) {
        return restaurantService.getRestaurant(restaurantId);
    }

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        return restaurantService.createRestaurant(request);
    }

    @PutMapping("/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public RestaurantResponse updateRestaurant(@PathVariable Long restaurantId,
                                                 @Valid @RequestBody UpdateRestaurantRequest request) {
        return restaurantService.updateRestaurant(restaurantId, request);
    }

    @PostMapping("/{restaurantId}/rating")
    @PreAuthorize("hasRole('USER')")
    public RatingResponse submitRating(@PathVariable Long restaurantId, @Valid @RequestBody RatingRequest request) {
        return restaurantService.submitRating(restaurantId, request);
    }
}

package com.tomato.service;

import com.tomato.dto.CreateRestaurantRequest;
import com.tomato.dto.RatingRequest;
import com.tomato.dto.RatingResponse;
import com.tomato.dto.RestaurantResponse;
import com.tomato.dto.UpdateRestaurantRequest;
import com.tomato.exception.BadRequestException;
import com.tomato.exception.ResourceNotFoundException;
import com.tomato.model.Order;
import com.tomato.model.OrderStatus;
import com.tomato.model.Rating;
import com.tomato.model.Restaurant;
import com.tomato.model.Role;
import com.tomato.model.User;
import com.tomato.repository.OrderRepository;
import com.tomato.repository.RatingRepository;
import com.tomato.repository.RestaurantRepository;
import com.tomato.security.SecurityUtils;
import com.tomato.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RatingRepository ratingRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        UserPrincipal current = SecurityUtils.getCurrentUser();
        if (current.getRole() != Role.RESTAURANT) {
            throw new AccessDeniedException("Only RESTAURANT users can register a restaurant");
        }

        User owner = userService.getUserEntityOrThrow(current.getId());
        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name(request.getName())
                .cuisine(request.getCuisine())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();

        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    public RestaurantResponse updateRestaurant(Long restaurantId, UpdateRestaurantRequest request) {
        Restaurant restaurant = getOwnedRestaurantOrThrow(restaurantId);
        restaurant.setName(request.getName());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setStatus(request.getStatus());
        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    public RestaurantResponse getRestaurant(Long restaurantId) {
        return RestaurantResponse.from(getRestaurantEntityOrThrow(restaurantId));
    }

    public List<RestaurantResponse> listRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(RestaurantResponse::from)
                .collect(Collectors.toList());
    }

    public List<RestaurantResponse> searchRestaurants(String query) {
        return restaurantRepository.search(query).stream()
                .map(RestaurantResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public RatingResponse submitRating(Long restaurantId, RatingRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Restaurant restaurant = getRestaurantEntityOrThrow(restaurantId);
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        if (!order.getCustomer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only rate your own orders");
        }
        if (!order.getRestaurant().getId().equals(restaurantId)) {
            throw new BadRequestException("Order " + order.getId() + " does not belong to restaurant " + restaurantId);
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Only delivered orders can be rated");
        }
        if (ratingRepository.existsByOrderId(order.getId())) {
            throw new BadRequestException("Order " + order.getId() + " has already been rated");
        }

        Rating rating = Rating.builder()
                .order(order)
                .restaurant(restaurant)
                .user(order.getCustomer())
                .score(request.getScore())
                .comment(request.getComment())
                .build();
        rating = ratingRepository.save(rating);

        recalculateRatingAggregate(restaurant);

        return RatingResponse.from(rating);
    }

    private void recalculateRatingAggregate(Restaurant restaurant) {
        List<Rating> ratings = ratingRepository.findByRestaurantId(restaurant.getId());
        double average = ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);

        restaurant.setAverageRating(BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
        restaurant.setRatingCount(ratings.size());
        restaurantRepository.save(restaurant);
    }

    public Restaurant getRestaurantEntityOrThrow(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
    }

    public Restaurant getOwnedRestaurantOrThrow(Long restaurantId) {
        Restaurant restaurant = getRestaurantEntityOrThrow(restaurantId);
        if (!restaurant.getOwner().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("You do not own restaurant " + restaurantId);
        }
        return restaurant;
    }
}

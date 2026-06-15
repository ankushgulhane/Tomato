package com.tomato.repository;

import com.tomato.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByRestaurantId(Long restaurantId);

    boolean existsByOrderId(Long orderId);
}

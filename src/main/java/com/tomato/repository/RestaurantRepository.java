package com.tomato.repository;

import com.tomato.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByOwnerId(Long ownerId);

    @Query("""
            select distinct r from Restaurant r
            left join MenuItem m on m.restaurant = r
            where lower(r.name) like lower(concat('%', :query, '%'))
               or lower(r.cuisine) like lower(concat('%', :query, '%'))
               or lower(r.address) like lower(concat('%', :query, '%'))
               or lower(m.name) like lower(concat('%', :query, '%'))
            """)
    List<Restaurant> search(@Param("query") String query);
}

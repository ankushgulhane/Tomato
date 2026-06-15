package com.tomato.config;

import com.tomato.model.MenuItem;
import com.tomato.model.Restaurant;
import com.tomato.model.Role;
import com.tomato.model.User;
import com.tomato.repository.MenuItemRepository;
import com.tomato.repository.RestaurantRepository;
import com.tomato.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the in-memory H2 database with sample users, restaurants, and menu items
 * so the API is immediately usable for manual testing and demos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${tomato.seed-data.enabled:true}")
    private boolean seedDataEnabled;

    @Override
    public void run(String... args) {
        if (!seedDataEnabled || userRepository.count() > 0) {
            return;
        }

        userRepository.save(buildUser("Admin User", "admin@tomato.com", Role.ADMIN, "555-0100", "1 Admin Plaza"));
        User alice = userRepository.save(buildUser("Alice Customer", "alice@tomato.com", Role.USER, "555-0111", "22 Maple Street"));
        User bob = userRepository.save(buildUser("Bob Rossi", "bob@tomato.com", Role.RESTAURANT, "555-0121", "12 Pasta Lane"));
        User mei = userRepository.save(buildUser("Mei Chen", "mei@tomato.com", Role.RESTAURANT, "555-0131", "8 Lotus Road"));
        User carol = userRepository.save(buildUser("Carol Driver", "carol@tomato.com", Role.DELIVERY_PARTNER, "555-0141", "5 Courier Court"));

        Restaurant tomatoBistro = restaurantRepository.save(Restaurant.builder()
                .owner(bob)
                .name("Tomato Italian Bistro")
                .cuisine("Italian")
                .address("12 Pasta Lane, Food City")
                .phone("555-0121")
                .build());

        Restaurant goldenDragon = restaurantRepository.save(Restaurant.builder()
                .owner(mei)
                .name("Golden Dragon")
                .cuisine("Chinese")
                .address("8 Lotus Road, Food City")
                .phone("555-0131")
                .build());

        seedMenu(tomatoBistro, List.of(
                menuItem("Margherita Pizza", "Classic pizza with tomato, mozzarella, and basil", "12.99", "Main"),
                menuItem("Spaghetti Carbonara", "Pasta with egg, pancetta, and parmesan", "13.49", "Main"),
                menuItem("Tiramisu", "Coffee-flavoured Italian dessert", "6.50", "Dessert")
        ));

        seedMenu(goldenDragon, List.of(
                menuItem("Kung Pao Chicken", "Stir-fried chicken with peanuts and chili peppers", "11.95", "Main"),
                menuItem("Spring Rolls", "Crispy vegetable spring rolls", "5.25", "Starter"),
                menuItem("Egg Fried Rice", "Wok-fried rice with egg and spring onion", "7.50", "Side")
        ));

        log.info("Seeded {} users and {} restaurants with sample data for local development.",
                userRepository.count(), restaurantRepository.count());
        log.debug("Customer login: alice@tomato.com / {}, Restaurant owner: bob@tomato.com / {}, " +
                        "Delivery partner: carol@tomato.com / {}",
                DEFAULT_PASSWORD, DEFAULT_PASSWORD, DEFAULT_PASSWORD);
        log.debug("Additional seeded accounts: admin@tomato.com, mei@tomato.com, delivery partner id {}", carol.getId());
        log.debug("Customer id {} ready to place orders", alice.getId());
    }

    private User buildUser(String name, String email, Role role, String phone, String address) {
        return User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(role)
                .phone(phone)
                .address(address)
                .build();
    }

    private MenuItem menuItem(String name, String description, String price, String category) {
        return MenuItem.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .category(category)
                .build();
    }

    private void seedMenu(Restaurant restaurant, List<MenuItem> items) {
        items.forEach(item -> {
            item.setRestaurant(restaurant);
            menuItemRepository.save(item);
        });
    }
}

package com.tomato.service;

import com.tomato.dto.CreateMenuItemRequest;
import com.tomato.dto.MenuItemResponse;
import com.tomato.dto.UpdateMenuItemRequest;
import com.tomato.exception.ResourceNotFoundException;
import com.tomato.model.MenuItem;
import com.tomato.model.Restaurant;
import com.tomato.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantService restaurantService;

    public List<MenuItemResponse> getMenu(Long restaurantId) {
        restaurantService.getRestaurantEntityOrThrow(restaurantId);
        return menuItemRepository.findByRestaurantIdAndActiveTrue(restaurantId).stream()
                .map(MenuItemResponse::from)
                .collect(Collectors.toList());
    }

    public MenuItemResponse addMenuItem(Long restaurantId, CreateMenuItemRequest request) {
        Restaurant restaurant = restaurantService.getOwnedRestaurantOrThrow(restaurantId);

        MenuItem menuItem = MenuItem.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .build();

        return MenuItemResponse.from(menuItemRepository.save(menuItem));
    }

    public MenuItemResponse updateMenuItem(Long restaurantId, Long menuItemId, UpdateMenuItemRequest request) {
        restaurantService.getOwnedRestaurantOrThrow(restaurantId);
        MenuItem menuItem = getMenuItemOrThrow(restaurantId, menuItemId);

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setActive(request.getActive());

        return MenuItemResponse.from(menuItemRepository.save(menuItem));
    }

    public void deactivateMenuItem(Long restaurantId, Long menuItemId) {
        restaurantService.getOwnedRestaurantOrThrow(restaurantId);
        MenuItem menuItem = getMenuItemOrThrow(restaurantId, menuItemId);
        menuItem.setActive(false);
        menuItemRepository.save(menuItem);
    }

    private MenuItem getMenuItemOrThrow(Long restaurantId, Long menuItemId) {
        return menuItemRepository.findByIdAndRestaurantId(menuItemId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu item " + menuItemId + " not found for restaurant " + restaurantId));
    }
}

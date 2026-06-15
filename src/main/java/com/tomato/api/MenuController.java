package com.tomato.api;

import com.tomato.dto.CreateMenuItemRequest;
import com.tomato.dto.MenuItemResponse;
import com.tomato.dto.UpdateMenuItemRequest;
import com.tomato.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public List<MenuItemResponse> getMenu(@PathVariable Long restaurantId) {
        return menuService.getMenu(restaurantId);
    }

    @PostMapping
    @PreAuthorize("hasRole('RESTAURANT')")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse addMenuItem(@PathVariable Long restaurantId,
                                          @Valid @RequestBody CreateMenuItemRequest request) {
        return menuService.addMenuItem(restaurantId, request);
    }

    @PutMapping("/{menuItemId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public MenuItemResponse updateMenuItem(@PathVariable Long restaurantId, @PathVariable Long menuItemId,
                                             @Valid @RequestBody UpdateMenuItemRequest request) {
        return menuService.updateMenuItem(restaurantId, menuItemId, request);
    }

    @DeleteMapping("/{menuItemId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateMenuItem(@PathVariable Long restaurantId, @PathVariable Long menuItemId) {
        menuService.deactivateMenuItem(restaurantId, menuItemId);
    }
}

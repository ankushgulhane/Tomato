package com.tomato.api;

import com.tomato.dto.RegisterUserRequest;
import com.tomato.dto.UserResponse;
import com.tomato.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;

    /**
     * Creates a user with any role, including RESTAURANT and DELIVERY_PARTNER.
     * Self-registration via /api/auth/register only allows the USER role -
     * onboarding restaurant owners and delivery partners is an admin action.
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody RegisterUserRequest request) {
        return authService.createUserAsAdmin(request);
    }
}

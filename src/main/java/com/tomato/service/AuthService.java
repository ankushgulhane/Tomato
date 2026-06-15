package com.tomato.service;

import com.tomato.dto.AuthResponse;
import com.tomato.dto.LoginRequest;
import com.tomato.dto.RegisterUserRequest;
import com.tomato.dto.UserResponse;
import com.tomato.exception.BadRequestException;
import com.tomato.exception.ResourceNotFoundException;
import com.tomato.model.Role;
import com.tomato.model.User;
import com.tomato.repository.UserRepository;
import com.tomato.security.JwtService;
import com.tomato.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterUserRequest request) {
        if (request.getRole() != Role.USER) {
            throw new BadRequestException(
                    "Self-registration is only available for customer accounts. " +
                            "Restaurant and delivery partner accounts must be created by an administrator.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    /**
     * Creates a user with an arbitrary role (RESTAURANT, DELIVERY_PARTNER, ADMIN, or USER).
     * Only reachable via the admin-only API - see AdminController.
     */
    public UserResponse createUserAsAdmin(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
        user = userRepository.save(user);

        return UserResponse.from(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(UserPrincipal.from(user));
        return new AuthResponse(token, jwtService.getExpirationDateTime(), user.getId());
    }
}

package com.tomato.service;

import com.tomato.dto.UserResponse;
import com.tomato.exception.ResourceNotFoundException;
import com.tomato.model.User;
import com.tomato.repository.UserRepository;
import com.tomato.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUser(Long userId) {
        SecurityUtils.requireSelfOrAdmin(userId);
        return UserResponse.from(getUserEntityOrThrow(userId));
    }

    public User getUserEntityOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
}

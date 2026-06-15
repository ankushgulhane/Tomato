package com.tomato.dto;

import com.tomato.model.Role;
import com.tomato.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final Role role;
    private final String phone;
    private final String address;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getAddress()
        );
    }
}

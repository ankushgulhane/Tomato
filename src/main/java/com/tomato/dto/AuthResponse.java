package com.tomato.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private final String token;
    private final LocalDateTime expiresAt;
    private final Long userId;
}

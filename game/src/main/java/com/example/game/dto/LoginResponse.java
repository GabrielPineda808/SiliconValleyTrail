package com.example.game.dto;

public record LoginResponse(
        String token,
        Long expiresIn
) {
}

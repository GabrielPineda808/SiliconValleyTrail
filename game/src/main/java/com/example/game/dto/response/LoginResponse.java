package com.example.game.dto.response;

public record LoginResponse(
        String token,
        Long expiresIn
) {
}

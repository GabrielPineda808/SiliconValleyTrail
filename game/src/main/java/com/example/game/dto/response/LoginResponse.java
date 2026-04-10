package com.example.game.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Authentication result returned after a successful login.
 */
@Schema(description = "JWT token and expiration metadata for an authenticated session.")
public record LoginResponse(
        @Schema(description = "Signed JWT bearer token.", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,

        @Schema(description = "Token lifetime in milliseconds.", example = "3600000")
        Long expiresIn
) {
}

package com.example.game.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response returned after a successful user registration.
 */
@Schema(description = "Summary of the newly created user account.")
public record RegisterUserResponse(
        @Schema(description = "Unique identifier of the created user.", example = "22")
        Long id,

        @Schema(description = "Username assigned to the new account.", example = "gabe")
        String username
) {
}

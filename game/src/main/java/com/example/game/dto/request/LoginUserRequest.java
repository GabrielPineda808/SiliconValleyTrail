package com.example.game.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload used to authenticate an existing user.
 */
@GroupSequence({
        LoginUserRequest.RequiredChecks.class,
        LoginUserRequest.FormatChecks.class,
        LoginUserRequest.class
})
@Schema(description = "Credentials required to authenticate a user.")
public record LoginUserRequest(
        @Schema(description = "Username of the existing account.", example = "gabe")
        @NotBlank(message = "Username is required", groups = RequiredChecks.class)
        @Size(min = 3, max = 15, message = "Username must be between 3 and 15 characters", groups = FormatChecks.class)
        String username,

        @Schema(description = "Plain text password for the existing account.", example = "password123")
        @NotBlank(message = "Password is required", groups = RequiredChecks.class)
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters", groups = FormatChecks.class)
        String password
) {
    /**
     * Validation group for required-field checks.
     */
    interface RequiredChecks {
    }

    /**
     * Validation group for format and length checks.
     */
    interface FormatChecks {
    }
}

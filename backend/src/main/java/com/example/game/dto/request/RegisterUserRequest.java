package com.example.game.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload used to create a new user account.
 */
@GroupSequence({
        RegisterUserRequest.RequiredChecks.class,
        RegisterUserRequest.FormatChecks.class,
        RegisterUserRequest.class
})
@Schema(description = "Credentials required to register a new user.")
public record RegisterUserRequest(
        @Schema(description = "Unique username for the new account.", example = "gabe")
        @NotBlank(message = "Username is required", groups = RequiredChecks.class)
        @Size(min = 3, max = 15, message = "Username must be between 3 and 15 characters", groups = FormatChecks.class)
        String username,

        @Schema(description = "Plain text password for the new account.", example = "password123")
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

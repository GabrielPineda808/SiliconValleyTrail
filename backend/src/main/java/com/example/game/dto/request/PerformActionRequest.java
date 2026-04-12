package com.example.game.dto.request;

import com.example.game.enums.ActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for executing a single turn action.
 */
@Schema(description = "Action the player wants to perform for the current turn.")
public record PerformActionRequest (
        @Schema(description = "Type of action to perform.", example = "TRAVEL")
        @NotNull(message = "Action is required")
        ActionType action
){
}

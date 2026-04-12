package com.example.game.dto.request;

import com.example.game.gameLogic.event.records.EventOptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for resolving the currently pending event.
 */
@Schema(description = "Choice selected by the player for the active event.")
public record ResolveEventRequest(
        @Schema(description = "Selected event option.", example = "ACCEPT")
        @NotNull(message = "Choice is required")
        EventOptionType choice
) {
}

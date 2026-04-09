package com.example.game.dto.request;

import com.example.game.gameLogic.event.records.EventOptionType;

public record ResolveEventRequest(
        EventOptionType choice
) {
}

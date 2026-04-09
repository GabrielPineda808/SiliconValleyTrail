package com.example.game.dto.response;

import com.example.game.enums.EventType;

public record EventResponse(
        EventType eventType,
        String title,
        String description
) {
}

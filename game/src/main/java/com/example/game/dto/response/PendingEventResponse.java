package com.example.game.dto.response;

import com.example.game.gameLogic.event.EventChoice;

import java.util.List;

public record PendingEventResponse(
        String type,
        String title,
        String description,
        List<EventChoice> choices
) {
}

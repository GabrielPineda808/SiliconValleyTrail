package com.example.game.gameLogic.event;

import java.util.List;

public record PendingEvent(
        String type,
        String title,
        String description,
        List<EventChoice> choices
) {
}

package com.example.game.gameLogic.event.records;

import com.example.game.enums.EventType;

import java.util.List;

public record PendingEvent(
        EventType type,
        String title,
        String description,
        List<EventOption> choices
) {
}

package com.example.game.dto.response;

import com.example.game.enums.EventType;
import com.example.game.gameLogic.event.records.EventChoice;
import com.example.game.gameLogic.event.records.EventOptionType;

import java.util.List;

public record PendingEventResponse(
        EventType type,
        String title,
        String description,
        List<EventChoice> choices
) {
}

package com.example.game.dto.response;

import com.example.game.enums.EventType;

import com.example.game.gameLogic.records.EventOption;
import com.example.game.gameLogic.records.PendingEvent;

import java.util.List;

public record PendingEventResponse(
        EventType type,
        String title,
        String description,
        List<EventOption> choices
) {
    public static PendingEventResponse from(PendingEvent pendingEvent) {
        if (pendingEvent == null) return null;

        return new PendingEventResponse(
                pendingEvent.type(),
                pendingEvent.title(),
                pendingEvent.description(),
                pendingEvent.choices().stream()
                        .map(option -> new EventOption(option.optionType(),option.label(), option.description() ))
                        .toList()
        );
    }
}

package com.example.game.gameLogic.event.records;

import com.example.game.enums.EventType;

import java.util.List;

public record EventResult(
        EventType eventType,
        String chosenOption,
        String outcome,
        List<String> effects
) {
}

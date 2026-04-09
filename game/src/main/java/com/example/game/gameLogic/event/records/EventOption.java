package com.example.game.gameLogic.event.records;

public record EventOption(
        EventOptionType optionType,
        String label,
        String description
) {
}

package com.example.game.gameLogic.event.records;

public record EventChoice(
        String code,
        EventOptionType choice
) {
    public static EventChoice from(EventOption option) {
        return new EventChoice(
                option.label(),
                option.optionType()
        );
    }
}

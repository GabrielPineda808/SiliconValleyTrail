package com.example.game.gameLogic.event.records;

import com.example.game.entity.GameState;
import com.example.game.enums.EventType;

import java.util.Optional;

public interface GameEvent {
    EventType getType();
    boolean canTrigger(GameState gameState);
    PendingEvent createPendingEvent(GameState gameState);
    EventResult resolve(GameState gameState, EventOptionType eventOptionType);
}

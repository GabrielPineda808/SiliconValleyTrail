package com.example.game.gameLogic.records;

import com.example.game.entity.GameState;
import com.example.game.enums.EventType;

public interface GameEvent {
    EventType getType();
    boolean canTrigger(GameState gameState);
    PendingEvent createPendingEvent(GameState gameState);
    EventResult resolve(GameState gameState, EventOptionType eventOptionType);
}

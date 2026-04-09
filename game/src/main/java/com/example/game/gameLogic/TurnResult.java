package com.example.game.gameLogic;

import com.example.game.gameLogic.action.ActionResult;
import com.example.game.entity.GameState;
import com.example.game.gameLogic.event.PendingEvent;

public record TurnResult(
        GameState gameState,
        ActionResult actionResult,
        PendingEvent pendingEvent
) {
}

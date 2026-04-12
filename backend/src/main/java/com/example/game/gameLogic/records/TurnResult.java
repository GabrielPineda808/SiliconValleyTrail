package com.example.game.gameLogic.records;

import com.example.game.gameLogic.action.ActionResult;
import com.example.game.entity.GameState;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TurnResult(
        GameState gameState,
        ActionResult actionResult,
        PendingEvent pendingEvent,
        String lossReason
) {
}

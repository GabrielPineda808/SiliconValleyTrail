package com.example.game.gameLogic.event;

import com.example.game.entity.GameState;
import com.example.game.gameLogic.action.ActionResult;

public record EventResolutionResult(
        GameState gameState,
        ActionResult actionResult
) {
}

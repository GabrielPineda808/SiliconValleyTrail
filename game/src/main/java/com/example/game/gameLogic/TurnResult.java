package com.example.game.gameLogic;

import com.example.game.gameLogic.action.ActionResult;
import com.example.game.entity.GameState;

public record TurnResult(
        GameState gameState,
        ActionResult actionResult
) {
}

package com.example.game.gameLogic.action;

import com.example.game.entity.GameState;

public interface PlayerAction {
    ActionType getType();
    boolean canExecute(GameState gameState);
    ActionResult execute(GameState gameState);
}

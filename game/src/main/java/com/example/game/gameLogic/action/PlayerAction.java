package com.example.game.gameLogic.action;

import com.example.game.entity.GameState;
import com.example.game.enums.ActionType;

public interface PlayerAction {
    ActionType getType();
    boolean canExecute(GameState gameState);
    ActionResult execute(GameState gameState);
}

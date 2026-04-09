package com.example.game.gameLogic.service;

import com.example.game.entity.GameState;
import com.example.game.enums.GameStatus;
import com.example.game.gameLogic.location.LocationRegistry;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class WinLossService {
    private final LocationRegistry locationRegistry;
    public void evaluate(GameState gameState) {
        if (isWon(gameState)) {
            gameState.setStatus(GameStatus.WON);
            return;
        }

        if (isLost(gameState)) {
            gameState.setStatus(GameStatus.LOST);
            return;
        }

        gameState.setStatus(GameStatus.IN_PROGRESS);
    }


    public boolean isWon(GameState gameState) {
        return gameState.getLocationIndex() == locationRegistry.lastIndex();
    }


    public boolean isLost(GameState gameState) {
        return gameState.getGas() <= 0
                || gameState.getCash() <= 0
                || gameState.getBugs() >= 15
                || gameState.getMotivation() <= 0
                || gameState.getCoffeeZeroStreak() >= 2;
    }


    public String getLossReason(GameState gameState) {
        if (gameState.getGas() <= 0) {
            return "You ran out of gas.";
        }
        if (gameState.getCash() <= 0) {
            return "You ran out of cash.";
        }
        if (gameState.getBugs() >= 15) {
            return "Too many bugs accumulated.";
        }
        if (gameState.getMotivation() <= 0) {
            return "The team lost all motivation.";
        }
        if (gameState.getCoffeeZeroStreak() >= 2) {
            return "The team went without coffee for too long.";
        }
        return null;
    }
}

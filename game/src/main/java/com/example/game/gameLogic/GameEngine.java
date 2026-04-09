package com.example.game.gameLogic;

import com.example.game.gameLogic.action.ActionResolver;
import com.example.game.gameLogic.action.ActionResult;
import com.example.game.gameLogic.action.PlayerAction;
import com.example.game.entity.GameState;
import com.example.game.enums.ActionType;
import com.example.game.enums.GameStatus;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.gameLogic.service.EventService;
import com.example.game.gameLogic.service.WinLossService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameEngine {

    private final ActionResolver actionResolver;
    private final EventService eventService;
    private final WinLossService winLossService;

    public TurnResult processTurn(GameState gameState, ActionType actionType) {
        if (gameState.getStatus() != GameStatus.IN_PROGRESS) {
            throw new InvalidActionException("Game is already finished.");
        }

        if (gameState.getPendingEventJson() != null) {
            throw new InvalidActionException("Resolve the current event before taking another action");
        }

        PlayerAction action = actionResolver.resolve(actionType);

        if (!action.canExecute(gameState)) {
            throw new InvalidActionException("Action cannot be executed with current resources.");
        }

        ActionResult actionResult = action.execute(gameState);
//        EventResult eventResult = null;

//        applyPassiveCosts(gameState);
        updateCoffeeZeroStreak(gameState);
        gameState.setDay(gameState.getDay() + 1);

//        if (actionResult.travelOccurred()) {
//            eventService.triggerArrivalEvents(gameState);
//        }

        winLossService.evaluate(gameState);

        return new TurnResult(gameState, actionResult);
    }

//    private void applyPassiveCosts(GameState gameState) {
//        gameState.setGas(gameState.getGas() - 5);
//        gameState.setCash(gameState.getCash() - 20);
//        gameState.setCoffee(gameState.getCoffee() - 3);
//        gameState.setMotivation(gameState.getMotivation() - 2);
//        gameState.setBugs(gameState.getBugs() + 1);
//    }

    private void updateCoffeeZeroStreak(GameState gameState) {
        if (gameState.getCoffee() <= 0) {
            gameState.setCoffeeZeroStreak(gameState.getCoffeeZeroStreak() + 1);
        } else {
            gameState.setCoffeeZeroStreak(0);
        }
    }
}

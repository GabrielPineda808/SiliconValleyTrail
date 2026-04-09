package com.example.game.gameLogic.action.rest;

import com.example.game.gameLogic.action.ActionResult;
import com.example.game.gameLogic.action.PlayerAction;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.entity.GameState;
import com.example.game.enums.ActionType;
import com.example.game.exceptions.InvalidActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RestAction implements PlayerAction {

    private static final int CASH_COST = 60;

    private final RandomProvider randomProvider;

    @Override
    public ActionType getType() {
        return ActionType.REST;
    }

    @Override
    public boolean canExecute(GameState gameState) {
        return gameState.getCash() >= CASH_COST;
    }

    @Override
    public ActionResult execute(GameState gameState) {
        if (!canExecute(gameState)) {
            throw new InvalidActionException("Cannot rest. Not enough cash.");
        }

        int coffeeRecovered = randomProvider.nextIntInclusive(5, 10);
        int motivationRecovered = randomProvider.nextIntInclusive(8, 15);

        gameState.setCash(gameState.getCash() - CASH_COST);
        gameState.setCoffee(gameState.getCoffee() + coffeeRecovered);
        gameState.setMotivation(gameState.getMotivation() + motivationRecovered);

        return new ActionResult(
                "The team took time to rest and recover.",
                List.of(
                        "-" + CASH_COST + " cash",
                        "+" + coffeeRecovered + " coffee",
                        "+" + motivationRecovered + " motivation"
                ),
                false
        );
    }
}

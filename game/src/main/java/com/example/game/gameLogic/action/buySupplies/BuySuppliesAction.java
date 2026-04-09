package com.example.game.gameLogic.action.buySupplies;

import com.example.game.gameLogic.RandomProvider;
import com.example.game.entity.GameState;
import com.example.game.enums.ActionType;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.gameLogic.action.ActionResult;
import com.example.game.gameLogic.action.PlayerAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BuySuppliesAction implements PlayerAction {

    private static final int CASH_COST = 75;

    private final RandomProvider randomProvider;

    @Override
    public ActionType getType() {
        return ActionType.BUY_SUPPLIES;
    }

    @Override
    public boolean canExecute(GameState gameState) {
        return gameState.getCash() >= CASH_COST;
    }

    @Override
    public ActionResult execute(GameState gameState) {
        if (!canExecute(gameState)) {
            throw new InvalidActionException("Cannot buy supplies. Not enough cash.");
        }

        int gasGained = randomProvider.nextIntInclusive(10, 18);
        int coffeeGained = randomProvider.nextIntInclusive(6, 12);

        gameState.setCash(gameState.getCash() - CASH_COST);
        gameState.setGas(gameState.getGas() + gasGained);
        gameState.setCoffee(gameState.getCoffee() + coffeeGained);

        return new ActionResult(
                "The team stopped to stock up on supplies.",
                List.of(
                        "-" + CASH_COST + " cash",
                        "+" + gasGained + " gas",
                        "+" + coffeeGained + " coffee"
                ),
                false
        );
    }
}

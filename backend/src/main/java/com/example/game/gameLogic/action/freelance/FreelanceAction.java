package com.example.game.gameLogic.action.freelance;

import com.example.game.gameLogic.action.ActionResult;
import com.example.game.gameLogic.action.PlayerAction;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.entity.GameState;
import com.example.game.gameLogic.action.ActionType;
import com.example.game.exceptions.InvalidActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FreelanceAction  implements PlayerAction {
    private static final int COFFEE_COST = 3;
    private static final int MOTIVATION_COST = 5;

    private final RandomProvider randomProvider;

    @Override
    public ActionType getType() {
        return ActionType.FREELANCE;
    }

    @Override
    public boolean canExecute(GameState gameState) {
        return gameState.getCoffee() >= COFFEE_COST
                && gameState.getMotivation() >= MOTIVATION_COST;
    }

    @Override
    public ActionResult execute(GameState gameState) {
        if (!canExecute(gameState)) {
            throw new InvalidActionException("Cannot freelance. Not enough coffee or motivation.");
        }

        int cashEarned = randomProvider.nextIntInclusive(80, 180);
        int coffeeSpent = Math.min(COFFEE_COST, gameState.getCoffee());

        gameState.setCoffee(gameState.getCoffee() - coffeeSpent);
        gameState.setMotivation(gameState.getMotivation() - MOTIVATION_COST);
        gameState.setCash(gameState.getCash() + cashEarned);

        return new ActionResult(
                "Freelance work brought in some cash.",
                List.of(
                        "+" + cashEarned + " cash",
                        "-" + coffeeSpent + " coffee",
                        "-" + MOTIVATION_COST + " motivation"
                ),
                false
        );
    }
}

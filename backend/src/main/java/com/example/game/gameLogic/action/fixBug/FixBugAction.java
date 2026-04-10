package com.example.game.gameLogic.action.fixBug;

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
public class FixBugAction implements PlayerAction {
    private static final int COFFEE_COST = 4;

    private final RandomProvider randomProvider;

    @Override
    public ActionType getType() {
        return ActionType.FIX_BUGS;
    }

    @Override
    public boolean canExecute(GameState gameState) {
        return gameState.getCoffee() >= COFFEE_COST && gameState.getBugs() > 0;
    }

    @Override
    public ActionResult execute(GameState gameState) {
        if (!canExecute(gameState)) {
            throw new InvalidActionException("Cannot fix bugs. Not enough coffee or no bugs to fix.");
        }

        int bugsFixed = randomProvider.nextIntInclusive(2, 4);
        int actualFixed = Math.min(bugsFixed, gameState.getBugs());

        gameState.setCoffee(gameState.getCoffee() - COFFEE_COST);
        gameState.setBugs(gameState.getBugs() - actualFixed);

        return new ActionResult(
                "The team fixed some bugs.",
                List.of(
                        "-" + COFFEE_COST + " coffee",
                        "-" + actualFixed + " bugs"
                ),
                false
        );
    }
}

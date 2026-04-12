package com.example.game.gameLogic.action.pitchVC;

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
public class PitchVcAction implements PlayerAction {

    private static final int MOTIVATION_COST = 12;
    private static final double SUCCESS_CHANCE = 0.40;

    private final RandomProvider randomProvider;

    @Override
    public ActionType getType() {
        return ActionType.PITCH_VC;
    }

    @Override
    public boolean canExecute(GameState gameState) {
        return gameState.getMotivation() >= MOTIVATION_COST;
    }

    @Override
    public ActionResult execute(GameState gameState) {
        if (!canExecute(gameState)) {
            throw new InvalidActionException("Cannot pitch VC. Not enough motivation.");
        }

        gameState.setMotivation(gameState.getMotivation() - MOTIVATION_COST);

        boolean success = randomProvider.chance(SUCCESS_CHANCE);

        if (success) {
            int cashAwarded = randomProvider.nextIntInclusive(250, 450);
            gameState.setCash(gameState.getCash() + cashAwarded);

            return new ActionResult(
                    "The VC pitch landed. Funding secured.",
                    List.of(
                            "+" + cashAwarded + " cash",
                            "-" + MOTIVATION_COST + " motivation"
                    ),
                    false
            );
        }

        return new ActionResult(
                "The VC pitch failed.",
                List.of(
                        "-" + MOTIVATION_COST + " motivation"
                ),
                false
        );
    }
}

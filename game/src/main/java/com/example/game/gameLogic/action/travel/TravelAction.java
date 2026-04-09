package com.example.game.gameLogic.action.travel;

import com.example.game.gameLogic.action.ActionResult;
import com.example.game.gameLogic.action.PlayerAction;
import com.example.game.entity.GameState;
import com.example.game.enums.ActionType;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.gameLogic.location.GameLocation;
import com.example.game.gameLogic.location.LocationRegistry;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class TravelAction implements PlayerAction {
    private final LocationRegistry locationRegistry;
    private final GasCalc gasCalc;

    @Override
    public ActionType getType() {
        return ActionType.TRAVEL;
    }

    @Override
    public boolean canExecute(GameState gameState) {
        if(gameState.getLocationIndex() >= locationRegistry.lastIndex()){
            return false;
        };
        GameLocation current = locationRegistry.getByIndex(gameState.getLocationIndex());
        GameLocation next = locationRegistry.getByIndex(gameState.getLocationIndex() + 1);

        int gasCost = gasCalc.calculateGasCost(
                current.latitude(),
                current.longitude(),
                next.latitude(),
                next.longitude()
        );

        return gameState.getGas() >= gasCost
                && gameState.getCoffee() >= 2
                && gameState.getMotivation() >= 3;
    }

    @Override
    public ActionResult execute(GameState gameState) {
        if(!canExecute(gameState)){
            throw new InvalidActionException("You can't execute this action");
        }
        GameLocation current = locationRegistry.getByIndex(gameState.getLocationIndex());
        GameLocation next = locationRegistry.getByIndex(gameState.getLocationIndex() + 1);

        int gasCost = gasCalc.calculateGasCost(
                current.latitude(),
                current.longitude(),
                next.latitude(),
                next.longitude()
        );

        int coffeeCost = 2;
        int motivationCost = 3;

        gameState.setGas(gameState.getGas() - gasCost);
        gameState.setCoffee(gameState.getCoffee() - coffeeCost);
        gameState.setMotivation(gameState.getMotivation() - motivationCost);
        gameState.setLocationIndex(gameState.getLocationIndex() + 1);
        gameState.setLocationName(next.name());

        return new ActionResult(
                "Traveled from " + current.name() + " to " + next.name(),
                List.of(
                        "-" + gasCost + " gas",
                        "-" + coffeeCost + " coffee",
                        "-" + motivationCost + " motivation"
                ),
                true
        );
    }
}

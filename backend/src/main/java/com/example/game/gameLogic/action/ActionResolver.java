package com.example.game.gameLogic.action;

import com.example.game.enums.ActionType;
import com.example.game.exceptions.InvalidActionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ActionResolver {

    private final List<PlayerAction> actions;

    public PlayerAction resolve(ActionType actionType) {
        return actions.stream()
                .filter(action -> action.getType() == actionType)
                .findFirst()
                .orElseThrow(() -> new InvalidActionException("Unsupported action: " + actionType));
    }
}

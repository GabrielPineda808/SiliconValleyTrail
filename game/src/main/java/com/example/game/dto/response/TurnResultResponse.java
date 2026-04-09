package com.example.game.dto.response;

import com.example.game.gameLogic.action.ActionResult;
import com.example.game.entity.GameState;
import com.example.game.enums.GameStatus;
import com.example.game.gameLogic.event.EventChoice;
import com.example.game.gameLogic.event.PendingEvent;

import java.util.List;

public record TurnResultResponse(
        Long gameId,
        Integer gas,
        Integer cash,
        Integer bugs,
        Integer coffee,
        Integer motivation,
        Integer locationIndex,
        String locationName,
        Integer day,
        GameStatus status,
        String message,
        List<String> effects,
        PendingEventResponse pendingEvent
) {
    public static TurnResultResponse from(
            GameState gameState,
            String locationName,
            ActionResult actionResult,
            PendingEvent pendingEvent
    ) {
        return new TurnResultResponse(
                gameState.getId(),
                gameState.getGas(),
                gameState.getCash(),
                gameState.getBugs(),
                gameState.getCoffee(),
                gameState.getMotivation(),
                gameState.getLocationIndex(),
                locationName,
                gameState.getDay(),
                gameState.getStatus(),
                actionResult.message(),
                actionResult.effects(),
                toPendingEventResponse(pendingEvent)

        );
    }

    private static PendingEventResponse toPendingEventResponse(PendingEvent pendingEvent) {
        if (pendingEvent == null) {
            return null;
        }

        return new PendingEventResponse(
                pendingEvent.type(),
                pendingEvent.title(),
                pendingEvent.description(),
                pendingEvent.choices().stream()
                        .map(choice -> new EventChoice(choice.code(), choice.choice()))
                        .toList()
        );
    }
}

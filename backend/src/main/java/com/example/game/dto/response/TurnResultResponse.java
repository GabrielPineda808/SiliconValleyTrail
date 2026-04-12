package com.example.game.dto.response;

import com.example.game.gameLogic.action.ActionResult;
import com.example.game.entity.GameState;
import com.example.game.enums.GameStatus;
import com.example.game.gameLogic.records.EventResult;
import com.example.game.gameLogic.records.PendingEvent;

import java.util.List;

public record TurnResultResponse(
        long gameId,
        int gas,
        int cash,
        int bugs,
        int coffee,
        int motivation,
        int locationIndex,
        String locationName,
        int day,
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
                PendingEventResponse.from(pendingEvent)
        );
    }



    public static TurnResultResponse fromResolvedEvent(
            GameState gameState,
            String locationName,
            EventResult eventResult
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
                eventResult.outcome(),
                eventResult.effects(),
                null
        );
    }
}

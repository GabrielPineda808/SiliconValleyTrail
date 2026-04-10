package com.example.game.dto.response;

import com.example.game.entity.GameState;
import com.example.game.enums.GameStatus;

public record GameStateResponse(
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
        String eventJson

) {
    public static GameStateResponse from(GameState gameState) {
        return new GameStateResponse(
                gameState.getId(),
                gameState.getGas(),
                gameState.getCash(),
                gameState.getBugs(),
                gameState.getCoffee(),
                gameState.getMotivation(),
                gameState.getLocationIndex(),
                gameState.getLocationName(),
                gameState.getDay(),
                gameState.getStatus(),
                gameState.getPendingEventJson()
        );
    }
}

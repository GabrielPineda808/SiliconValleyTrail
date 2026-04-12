package com.example.game.dto.response;

import com.example.game.entity.GameState;
import com.example.game.enums.GameStatus;
import com.example.game.gameLogic.event.records.PendingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public record GameStateResponse(
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
        PendingEventResponse eventJson

) {
    private static final ObjectMapper objectMapper = new ObjectMapper();
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
                toPendingEventResponse(gameState.getPendingEventJson())
        );
    }

    private static PendingEventResponse toPendingEventResponse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            PendingEvent pendingEvent = objectMapper.readValue(json, PendingEvent.class);
            return PendingEventResponse.from(pendingEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid pending event JSON", e);
        }
    }

}

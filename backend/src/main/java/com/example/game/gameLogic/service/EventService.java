package com.example.game.gameLogic.service;


import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.records.EventOptionType;
import com.example.game.gameLogic.records.EventResult;
import com.example.game.gameLogic.records.GameEvent;
import com.example.game.gameLogic.records.PendingEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@AllArgsConstructor
public class EventService {
    private final List<GameEvent> gameEvents;
    private final RandomProvider randomProvider;
    private final ObjectMapper objectMapper;

    public PendingEvent triggerArrivalEvent(GameState gameState) {

        List<GameEvent> eligibleEvents = gameEvents.stream().filter(gameEvent -> gameEvent.canTrigger(gameState)).toList();

        if (eligibleEvents.isEmpty()) {
            return null;
        }

        GameEvent selected = eligibleEvents.get(
                randomProvider.nextIntInclusive(0, eligibleEvents.size() - 1)
        );

        PendingEvent result = selected.createPendingEvent(gameState);
        System.out.println(result);

        savePendingEvent(gameState,result);

        return result;
    }

    public EventResult resolvePendingEvent(GameState gameState, EventOptionType chosenOption) {
        if (!gameState.isEventPending() || gameState.getPendingEventJson() == null) {
            throw new InvalidActionException("There is no pending event to resolve.");
        }

        PendingEvent pendingEvent = readPendingEvent(gameState);

        validateChosenOptionExists(pendingEvent, chosenOption);

        GameEvent gameEvent = loadEvent(pendingEvent.type());

        EventResult result = gameEvent.resolve(gameState, chosenOption);

        clearPendingEvent(gameState);

        return result;}

    private void validateChosenOptionExists(PendingEvent pendingEvent, EventOptionType chosenOption) {
        boolean valid = pendingEvent.choices().stream()
                .anyMatch(option -> option.optionType() == chosenOption);

        if (!valid) {
            throw new InvalidActionException("Invalid option for pending event: " + chosenOption);
        }
    }

    private void savePendingEvent(GameState gameState, PendingEvent event) {
        try {
            gameState.setPendingEventJson(objectMapper.writeValueAsString(event));
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize pending event", e);
        }

        gameState.setEventPending(true);
    }

    private GameEvent loadEvent(EventType eventType) {
        return gameEvents.stream()
                .filter(event -> event.getType() == eventType)
                .findFirst()
                .orElseThrow(() -> new InvalidActionException("No event handler found for event type: " + eventType));
    }


    private PendingEvent readPendingEvent(GameState gameState) {
        if (gameState.getPendingEventJson() == null || gameState.getPendingEventJson().isBlank()) {
            throw new InvalidActionException("Pending event payload is missing.");
        }

        try {
            return objectMapper.readValue(gameState.getPendingEventJson(), PendingEvent.class);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to deserialize pending event", e);
        }
    }

    private void clearPendingEvent(GameState gameState) {
        gameState.setPendingEventJson(null);
        gameState.setEventPending(false);
    }
}
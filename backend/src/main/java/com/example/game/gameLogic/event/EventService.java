package com.example.game.gameLogic.event;

import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.event.records.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EventService {
    private final List<GameEvent> gameEvents;
    private final RandomProvider randomProvider;
    private final ObjectMapper objectMapper;

    public PendingEvent triggerArrivalEvent(GameState gameState) {
        if(gameState.getPendingEventType() != null){
            return null;
        }

        List<GameEvent> eligibleEvents = gameEvents.stream().filter(gameEvent -> gameEvent.canTrigger(gameState)).toList();

        if (eligibleEvents.isEmpty()) {
            return null;
        }

        GameEvent selected = eligibleEvents.get(
                randomProvider.nextIntInclusive(0, eligibleEvents.size() - 1)
        );

        PendingEvent result = selected.createPendingEvent(gameState);

        persistPendingEvent(gameState,result);

        return result;
    }

    public EventResult resolvePendingEvent(GameState gameState, EventOptionType chosenOption) {
        if (!gameState.isEventPending() || gameState.getPendingEventType() == null) {
            throw new InvalidActionException("There is no pending event to resolve.");
        }

        PendingEvent pendingEvent = readPendingEvent(gameState);

        validateOptionExists(pendingEvent, chosenOption);

        GameEvent gameEvent = getEventHandler(gameState.getPendingEventType());

        EventResult result = gameEvent.resolve(gameState, chosenOption);

        clearPendingEvent(gameState);

        return result;}

    private void validateOptionExists(PendingEvent pendingEvent, EventOptionType chosenOption) {
        boolean valid = pendingEvent.choices().stream()
                .map(EventOption::optionType)
                .anyMatch(option -> option == chosenOption);

        if (!valid) {
            throw new InvalidActionException("Invalid option for pending event: " + chosenOption);
        }
    }

    private void persistPendingEvent(GameState gameState, PendingEvent event) {
        gameState.setPendingEventType(event.type());

        try {
            gameState.setPendingEventJson(objectMapper.writeValueAsString(event));
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize pending event", e);
        }

        gameState.setEventPending(true);
    }

    private GameEvent getEventHandler(EventType eventType) {
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
        gameState.setPendingEventType(null);
        gameState.setPendingEventJson(null);
        gameState.setEventPending(false);
    }
}

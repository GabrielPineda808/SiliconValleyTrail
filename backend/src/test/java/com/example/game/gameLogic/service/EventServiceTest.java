package com.example.game.gameLogic.service;

import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.enums.GameStatus;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.records.EventOption;
import com.example.game.gameLogic.records.EventOptionType;
import com.example.game.gameLogic.records.EventResult;
import com.example.game.gameLogic.records.GameEvent;
import com.example.game.gameLogic.records.PendingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private RandomProvider randomProvider;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private GameEvent firstEvent;

    @Mock
    private GameEvent secondEvent;

    @InjectMocks
    private EventService eventService;

    @Test
    void triggerArrivalEventReturnsNullWhenNoEventsCanTrigger() {
        GameState gameState = baseState();
        eventService = new EventService(List.of(firstEvent, secondEvent), randomProvider, objectMapper);
        when(firstEvent.canTrigger(gameState)).thenReturn(false);
        when(secondEvent.canTrigger(gameState)).thenReturn(false);

        PendingEvent pendingEvent = eventService.triggerArrivalEvent(gameState);

        assertThat(pendingEvent).isNull();
        assertThat(gameState.isEventPending()).isFalse();
        assertThat(gameState.getPendingEventJson()).isNull();
    }

    @Test
    void triggerArrivalEventSelectsEligibleEventAndPersistsPendingJson() throws Exception {
        GameState gameState = baseState();
        PendingEvent pendingEvent = pendingEvent(EventType.TECH_RECRUITER, EventOptionType.ACCEPT, EventOptionType.DECLINE);
        eventService = new EventService(List.of(firstEvent, secondEvent), randomProvider, objectMapper);

        when(firstEvent.canTrigger(gameState)).thenReturn(false);
        when(secondEvent.canTrigger(gameState)).thenReturn(true);
        when(randomProvider.nextIntInclusive(0, 0)).thenReturn(0);
        when(secondEvent.createPendingEvent(gameState)).thenReturn(pendingEvent);
        when(objectMapper.writeValueAsString(pendingEvent)).thenReturn("{\"type\":\"TECH_RECRUITER\"}");

        PendingEvent result = eventService.triggerArrivalEvent(gameState);

        assertThat(result).isEqualTo(pendingEvent);
        assertThat(gameState.isEventPending()).isTrue();
        assertThat(gameState.getPendingEventJson()).isEqualTo("{\"type\":\"TECH_RECRUITER\"}");
    }

    @Test
    void resolvePendingEventRejectsMissingPendingState() {
        GameState gameState = baseState();
        eventService = new EventService(List.of(firstEvent), randomProvider, objectMapper);

        assertThatThrownBy(() -> eventService.resolvePendingEvent(gameState, EventOptionType.ACCEPT))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("There is no pending event to resolve.");
    }

    @Test
    void resolvePendingEventRejectsChoiceThatIsNotInPendingOptions() throws Exception {
        GameState gameState = baseState();
        PendingEvent pendingEvent = pendingEvent(EventType.TECH_RECRUITER, EventOptionType.ACCEPT);
        gameState.setEventPending(true);
        gameState.setPendingEventJson("{\"type\":\"TECH_RECRUITER\"}");
        eventService = new EventService(List.of(firstEvent), randomProvider, objectMapper);

        when(objectMapper.readValue(gameState.getPendingEventJson(), PendingEvent.class)).thenReturn(pendingEvent);

        assertThatThrownBy(() -> eventService.resolvePendingEvent(gameState, EventOptionType.DECLINE))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("Invalid option for pending event: DECLINE");
    }

    @Test
    void resolvePendingEventClearsPendingStateAndDelegatesToMatchingEvent() throws Exception {
        GameState gameState = baseState();
        PendingEvent pendingEvent = pendingEvent(EventType.COFFEE_SHOP_DEAL, EventOptionType.TAKE_DEAL, EventOptionType.PASS);
        EventResult eventResult = new EventResult(
                EventType.COFFEE_SHOP_DEAL,
                "TAKE_DEAL",
                "You took the deal and restocked the team’s fuel.",
                List.of("-30 cash", "+10 coffee")
        );
        gameState.setEventPending(true);
        gameState.setPendingEventJson("{\"type\":\"COFFEE_SHOP_DEAL\"}");
        eventService = new EventService(List.of(firstEvent), randomProvider, objectMapper);

        when(objectMapper.readValue(gameState.getPendingEventJson(), PendingEvent.class)).thenReturn(pendingEvent);
        when(firstEvent.getType()).thenReturn(EventType.COFFEE_SHOP_DEAL);
        when(firstEvent.resolve(gameState, EventOptionType.TAKE_DEAL)).thenReturn(eventResult);

        EventResult result = eventService.resolvePendingEvent(gameState, EventOptionType.TAKE_DEAL);

        assertThat(result).isEqualTo(eventResult);
        assertThat(gameState.isEventPending()).isFalse();
        assertThat(gameState.getPendingEventJson()).isNull();
        verify(firstEvent).resolve(gameState, EventOptionType.TAKE_DEAL);
    }

    @Test
    void resolvePendingEventFailsWhenHandlerForPendingTypeIsMissing() throws Exception {
        GameState gameState = baseState();
        PendingEvent pendingEvent = pendingEvent(EventType.VIRAL_TWEET, EventOptionType.ACCEPT);
        gameState.setEventPending(true);
        gameState.setPendingEventJson("{\"type\":\"VIRAL_TWEET\"}");
        eventService = new EventService(List.of(firstEvent), randomProvider, objectMapper);

        when(objectMapper.readValue(gameState.getPendingEventJson(), PendingEvent.class)).thenReturn(pendingEvent);
        when(firstEvent.getType()).thenReturn(EventType.TECH_RECRUITER);

        assertThatThrownBy(() -> eventService.resolvePendingEvent(gameState, EventOptionType.ACCEPT))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("No event handler found for event type: VIRAL_TWEET");
    }

    @Test
    void triggerArrivalEventWrapsSerializationFailures() throws Exception {
        GameState gameState = baseState();
        PendingEvent pendingEvent = pendingEvent(EventType.TECH_RECRUITER, EventOptionType.ACCEPT);
        eventService = new EventService(List.of(firstEvent), randomProvider, objectMapper);

        when(firstEvent.canTrigger(gameState)).thenReturn(true);
        when(randomProvider.nextIntInclusive(0, 0)).thenReturn(0);
        when(firstEvent.createPendingEvent(gameState)).thenReturn(pendingEvent);
        when(objectMapper.writeValueAsString(any(PendingEvent.class))).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> eventService.triggerArrivalEvent(gameState))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");
    }

    private static PendingEvent pendingEvent(EventType type, EventOptionType... options) {
        List<EventOption> choices = java.util.Arrays.stream(options)
                .map(option -> new EventOption(option, option.name(), option.name()))
                .toList();
        return new PendingEvent(type, type.name(), "desc", choices);
    }

    private static GameState baseState() {
        return GameState.builder()
                .gas(100)
                .cash(500)
                .bugs(2)
                .coffee(10)
                .motivation(20)
                .locationIndex(1)
                .locationName("Santa Clara")
                .day(2)
                .status(GameStatus.IN_PROGRESS)
                .coffeeZeroStreak(0)
                .eventPending(false)
                .build();
    }
}

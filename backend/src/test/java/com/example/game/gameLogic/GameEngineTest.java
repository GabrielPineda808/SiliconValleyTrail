package com.example.game.gameLogic;

import com.example.game.entity.GameState;
import com.example.game.enums.ActionType;
import com.example.game.enums.EventType;
import com.example.game.enums.GameStatus;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.gameLogic.action.ActionResolver;
import com.example.game.gameLogic.action.ActionResult;
import com.example.game.gameLogic.action.PlayerAction;
import com.example.game.gameLogic.event.EventService;
import com.example.game.gameLogic.event.records.EventOption;
import com.example.game.gameLogic.event.records.EventOptionType;
import com.example.game.gameLogic.event.records.PendingEvent;
import com.example.game.gameLogic.service.WinLossService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameEngineTest {

    @Mock
    private ActionResolver actionResolver;

    @Mock
    private EventService eventService;

    @Mock
    private WinLossService winLossService;

    @Mock
    private PlayerAction playerAction;

    @InjectMocks
    private GameEngine gameEngine;

    @Test
    void processTurnRejectsFinishedGames() {
        GameState gameState = baseGameState();
        gameState.setStatus(GameStatus.WON);

        assertThatThrownBy(() -> gameEngine.processTurn(gameState, ActionType.REST))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("Game is already finished.");
    }

    @Test
    void processTurnRejectsActionWhilePendingEventExists() {
        GameState gameState = baseGameState();
        gameState.setPendingEventJson("{\"type\":\"VC_FUNDING_OFFER\"}");

        assertThatThrownBy(() -> gameEngine.processTurn(gameState, ActionType.REST))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("Resolve the current event before taking another action");
    }

    @Test
    void processTurnRejectsUnexecutableActions() {
        GameState gameState = baseGameState();
        when(actionResolver.resolve(ActionType.FIX_BUGS)).thenReturn(playerAction);
        when(playerAction.canExecute(gameState)).thenReturn(Boolean.valueOf(false));

        assertThatThrownBy(() -> gameEngine.processTurn(gameState, ActionType.FIX_BUGS))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("Action cannot be executed with current resources.");
    }

    @Test
    void processTurnAdvancesDayResetsCoffeeStreakAndDoesNotTriggerEventForNonTravel() {
        GameState gameState = baseGameState();
        gameState.setCoffee(Integer.valueOf(10));
        gameState.setCoffeeZeroStreak(Integer.valueOf(3));
        ActionResult actionResult = new ActionResult("Rested", List.of("+10 motivation"), false);

        when(actionResolver.resolve(ActionType.REST)).thenReturn(playerAction);
        when(playerAction.canExecute(gameState)).thenReturn(Boolean.valueOf(true));
        when(playerAction.execute(gameState)).thenReturn(actionResult);

        TurnResult result = gameEngine.processTurn(gameState, ActionType.REST);

        verify(winLossService).evaluate(gameState);
        verify(eventService, never()).triggerArrivalEvent(gameState);
        assertThat(gameState.getDay()).isEqualTo(2);
        assertThat(gameState.getCoffeeZeroStreak()).isZero();
        assertThat(result.actionResult()).isEqualTo(actionResult);
        assertThat(result.pendingEvent()).isNull();
    }

    @Test
    void processTurnTriggersArrivalEventForTravelAndTracksCoffeeDepletion() {
        GameState gameState = baseGameState();
        gameState.setCoffee(Integer.valueOf(0));
        gameState.setCoffeeZeroStreak(Integer.valueOf(1));
        ActionResult actionResult = new ActionResult("Traveled", List.of("-20 gas"), true);
        PendingEvent pendingEvent = new PendingEvent(
                EventType.FLIGHT_DROP,
                "Cheap flight",
                "A discount appears",
                List.of(new EventOption(EventOptionType.TAKE_DEAL, "Take it", "Book now"))
        );

        when(actionResolver.resolve(ActionType.TRAVEL)).thenReturn(playerAction);
        when(playerAction.canExecute(gameState)).thenReturn(Boolean.valueOf(true));
        when(playerAction.execute(gameState)).thenReturn(actionResult);
        when(eventService.triggerArrivalEvent(gameState)).thenReturn(pendingEvent);

        TurnResult result = gameEngine.processTurn(gameState, ActionType.TRAVEL);

        verify(winLossService).evaluate(gameState);
        verify(eventService).triggerArrivalEvent(gameState);
        assertThat(gameState.getDay()).isEqualTo(2);
        assertThat(gameState.getCoffeeZeroStreak()).isEqualTo(2);
        assertThat(result.pendingEvent()).isEqualTo(pendingEvent);
    }

    private static GameState baseGameState() {
        return GameState.builder()
                .gas(Integer.valueOf(100))
                .cash(Integer.valueOf(500))
                .bugs(Integer.valueOf(0))
                .coffee(Integer.valueOf(50))
                .motivation(Integer.valueOf(100))
                .locationIndex(Integer.valueOf(0))
                .locationName("San Jose")
                .day(Integer.valueOf(1))
                .status(GameStatus.IN_PROGRESS)
                .coffeeZeroStreak(Integer.valueOf(0))
                .eventPending(false)
                .build();
    }
}

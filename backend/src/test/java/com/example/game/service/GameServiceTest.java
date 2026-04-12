package com.example.game.service;

import com.example.game.dto.response.TurnResultResponse;
import com.example.game.entity.GameState;
import com.example.game.entity.User;
import com.example.game.gameLogic.action.ActionType;
import com.example.game.enums.EventType;
import com.example.game.enums.GameStatus;
import com.example.game.exceptions.GameExistsException;
import com.example.game.exceptions.NotFoundException;
import com.example.game.gameLogic.GameEngine;
import com.example.game.gameLogic.records.TurnResult;
import com.example.game.gameLogic.action.ActionResult;
import com.example.game.gameLogic.records.EventOption;
import com.example.game.gameLogic.records.EventOptionType;
import com.example.game.gameLogic.records.EventResult;
import com.example.game.gameLogic.records.PendingEvent;
import com.example.game.gameLogic.location.GameLocation;
import com.example.game.gameLogic.location.LocationRegistry;
import com.example.game.gameLogic.service.EventService;
import com.example.game.gameLogic.service.WinLossService;
import com.example.game.repository.GameStateRepo;
import com.example.game.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private GameStateRepo gameStateRepo;

    @Mock
    private GameEngine gameEngine;

    @Mock
    private LocationRegistry locationRegistry;

    @Mock
    private EventService eventService;

    @Mock
    private WinLossService winLossService;

    @InjectMocks
    private GameService gameService;

//    @Test
//    void createGameBuildsAndSavesNewStateWhenNoActiveGameExists() {
//        User user = buildUser();
//        when(userRepo.findByUsername("gabe")).thenReturn(Optional.of(user));
//        when(gameStateRepo.existsByUserId(1L)).thenReturn(false);
//        when(gameStateRepo.save(any(GameState.class))).thenAnswer(invocation -> {
//            GameState state = invocation.getArgument(0);
//            state.setId(99L);
//            return state;
//        });
//
//        GameState result = gameService.createGame("gabe");
//
//        assertThat(result.getId()).isEqualTo(99L);
//        assertThat(result.getUser()).isSameAs(user);
//        assertThat(result.getGas()).isEqualTo(100);
//        assertThat(result.getCash()).isEqualTo(500);
//        assertThat(result.getLocationName()).isEqualTo("San Jose");
//        assertThat(result.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
//    }

    @Test
    void createGameThrowsWhenActiveGameAlreadyExists() {
        User user = buildUser();
        when(userRepo.findByUsername("gabe")).thenReturn(Optional.of(user));
        when(gameStateRepo.existsByUserId(1L)).thenReturn(true);
        when(gameStateRepo.existsByUserAndStatus(user, GameStatus.IN_PROGRESS)).thenReturn(true);

        assertThatThrownBy(() -> gameService.createGame("gabe"))
                .isInstanceOf(GameExistsException.class)
                .hasMessage("Game exists for this user");
    }

    @Test
    void performActionReturnsMappedTurnResult() {
        User user = buildUser();
        GameState activeGame = buildActiveGame(user);
        ActionResult actionResult = new ActionResult("Moved forward", List.of("+1 day"), true);
        PendingEvent pendingEvent = new PendingEvent(
                EventType.VC_FUNDING_OFFER,
                "Pitch deck",
                "A VC wants to invest",
                List.of(
                        new EventOption(EventOptionType.ACCEPT, "Accept", "Take the money"),
                        new EventOption(EventOptionType.DECLINE, "Decline", "Stay lean")
                )
        );
        TurnResult turnResult = new TurnResult(activeGame, actionResult, pendingEvent, "");

        when(userRepo.findByUsername("gabe")).thenReturn(Optional.of(user));
        when(gameStateRepo.findStateByUserAndStatus(user, GameStatus.IN_PROGRESS)).thenReturn(Optional.of(activeGame));
        when(gameEngine.processTurn(activeGame, ActionType.TRAVEL)).thenReturn(turnResult);
        when(gameStateRepo.save(activeGame)).thenReturn(activeGame);
        when(locationRegistry.getByIndex(activeGame.getLocationIndex()))
                .thenReturn(new GameLocation( "San Francisco", 0, 0));

        TurnResultResponse response = gameService.performAction("gabe", ActionType.TRAVEL);

        assertThat(response.locationName()).isEqualTo("San Francisco");
        assertThat(response.message()).isEqualTo("Moved forward");
        assertThat(response.effects()).containsExactly("+1 day");
        assertThat(response.pendingEvent()).isNotNull();
        assertThat(response.pendingEvent().type()).isEqualTo(EventType.VC_FUNDING_OFFER);
        assertThat(response.pendingEvent().choices())
                .containsExactly(
                        new EventOption("Accept", EventOptionType.ACCEPT),
                        new EventOption("Decline", EventOptionType.DECLINE)
                );
    }

    @Test
    void performActionThrowsWhenThereIsNoActiveGame() {
        User user = buildUser();
        when(userRepo.findByUsername("gabe")).thenReturn(Optional.of(user));
        when(gameStateRepo.findStateByUserAndStatus(user, GameStatus.IN_PROGRESS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.performAction("gabe", ActionType.REST))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No active game found.");
    }

    @Test
    void resolvePendingEventEvaluatesStateAndReturnsResolvedResponse() {
        User user = buildUser();
        GameState activeGame = buildActiveGame(user);
        EventResult eventResult = new EventResult(
                EventType.VIRAL_TWEET,
                "INVEST",
                "The tweet takes off",
                List.of("+200 cash", "+10 motivation")
        );

        when(userRepo.findByUsername("gabe")).thenReturn(Optional.of(user));
        when(gameStateRepo.findStateByUserAndStatus(user, GameStatus.IN_PROGRESS)).thenReturn(Optional.of(activeGame));
        when(eventService.resolvePendingEvent(activeGame, EventOptionType.INVEST)).thenReturn(eventResult);
        when(gameStateRepo.save(activeGame)).thenReturn(activeGame);

        TurnResultResponse response = gameService.resolvePendingEvent("gabe", EventOptionType.INVEST);

        verify(winLossService).evaluate(activeGame);
        assertThat(response.locationName()).isEqualTo(activeGame.getLocationName());
        assertThat(response.message()).isEqualTo("The tweet takes off");
        assertThat(response.effects()).containsExactly("+200 cash", "+10 motivation");
        assertThat(response.pendingEvent()).isNull();
    }

    private static User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("gabe");
        user.setPasswordHash("encoded-password");
        return user;
    }

    private static GameState buildActiveGame(User user) {
        return GameState.builder()
                .id(5L)
                .user(user)
                .gas(90)
                .cash(600)
                .bugs(2)
                .coffee(30)
                .motivation(75)
                .locationIndex(1)
                .locationName("San Jose")
                .day(3)
                .status(GameStatus.IN_PROGRESS)
                .coffeeZeroStreak(0)
                .eventPending(false)
                .build();
    }
}

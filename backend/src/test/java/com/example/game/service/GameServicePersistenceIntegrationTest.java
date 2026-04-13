package com.example.game.service;

import com.example.game.dto.response.TurnResultResponse;
import com.example.game.entity.GameState;
import com.example.game.entity.User;
import com.example.game.enums.EventType;
import com.example.game.enums.GameStatus;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.gameLogic.GameEngine;
import com.example.game.gameLogic.action.ActionResult;
import com.example.game.gameLogic.action.ActionType;
import com.example.game.gameLogic.location.GameLocation;
import com.example.game.gameLogic.location.LocationRegistry;
import com.example.game.gameLogic.records.EventOptionType;
import com.example.game.gameLogic.records.EventResult;
import com.example.game.gameLogic.records.TurnResult;
import com.example.game.gameLogic.service.EventService;
import com.example.game.gameLogic.service.WinLossService;
import com.example.game.repository.GameStateRepo;
import com.example.game.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class GameServicePersistenceIntegrationTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GameStateRepo gameStateRepo;

    @MockitoBean
    private GameEngine gameEngine;

    @MockitoBean
    private LocationRegistry locationRegistry;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private WinLossService winLossService;

    @BeforeEach
    void cleanDatabase() {
        gameStateRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void performActionPersistsMutatedGameStateAndReturnsMappedResponse() {
        User user = userRepo.save(user("gabe"));
        GameState activeGame = gameStateRepo.save(activeGame(user));

        doAnswer(invocation -> {
            GameState state = invocation.getArgument(0);
            state.setGas(88);
            state.setCoffee(48);
            state.setMotivation(97);
            state.setLocationIndex(1);
            state.setLocationName("Santa Clara");
            state.setDay(2);
            return new TurnResult(
                    state,
                    new ActionResult("Traveled from San Jose to Santa Clara", List.of("-12 gas"), true),
                    null,
                    null
            );
        }).when(gameEngine).processTurn(any(GameState.class), any(ActionType.class));
        when(locationRegistry.getByIndex(1)).thenReturn(new GameLocation("Santa Clara", 0, 0));

        TurnResultResponse response = gameService.performAction("gabe", ActionType.TRAVEL);

        GameState reloaded = gameStateRepo.findStateByUserAndStatus(user, GameStatus.IN_PROGRESS).orElseThrow();
        assertThat(reloaded.getGas()).isEqualTo(88);
        assertThat(reloaded.getCoffee()).isEqualTo(48);
        assertThat(reloaded.getMotivation()).isEqualTo(97);
        assertThat(reloaded.getLocationName()).isEqualTo("Santa Clara");
        assertThat(reloaded.getDay()).isEqualTo(2);
        assertThat(response.locationName()).isEqualTo("Santa Clara");
        assertThat(response.message()).isEqualTo("Traveled from San Jose to Santa Clara");
    }

    @Test
    void performActionRollsBackManagedEntityMutationWhenEngineThrows() {
        User user = userRepo.save(user("gabe"));
        gameStateRepo.save(activeGame(user));

        doAnswer(invocation -> {
            GameState state = invocation.getArgument(0);
            state.setCash(1);
            throw new InvalidActionException("boom");
        }).when(gameEngine).processTurn(any(GameState.class), any(ActionType.class));

        assertThatThrownBy(() -> gameService.performAction("gabe", ActionType.REST))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("boom");

        GameState reloaded = gameStateRepo.findStateByUserAndStatus(user, GameStatus.IN_PROGRESS).orElseThrow();
        assertThat(reloaded.getCash()).isEqualTo(500);
    }

    @Test
    void resolvePendingEventPersistsClearedPendingStateAndUpdatedResources() {
        User user = userRepo.save(user("gabe"));
        GameState activeGame = activeGame(user);
        activeGame.setEventPending(true);
        activeGame.setPendingEventJson("{\"type\":\"VC_FUNDING_OFFER\"}");
        gameStateRepo.save(activeGame);

        doAnswer(invocation -> {
            GameState state = invocation.getArgument(0);
            state.setEventPending(false);
            state.setPendingEventJson(null);
            state.setCash(state.getCash() + 200);
            return new EventResult(
                    EventType.VC_FUNDING_OFFER,
                    "ACCEPT",
                    "The pitch worked. Funding came through.",
                    List.of("+200 cash")
            );
        }).when(eventService).resolvePendingEvent(any(GameState.class), any(EventOptionType.class));

        TurnResultResponse response = gameService.resolvePendingEvent("gabe", EventOptionType.ACCEPT);

        GameState reloaded = gameStateRepo.findStateByUserAndStatus(user, GameStatus.IN_PROGRESS).orElseThrow();
        assertThat(reloaded.isEventPending()).isFalse();
        assertThat(reloaded.getPendingEventJson()).isNull();
        assertThat(reloaded.getCash()).isEqualTo(700);
        assertThat(response.message()).isEqualTo("The pitch worked. Funding came through.");
        verify(winLossService).evaluate(any(GameState.class));
    }

    private static User user(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvabcdefghijklmnopqrstuvabcdefgh");
        return user;
    }

    private static GameState activeGame(User user) {
        return GameState.builder()
                .user(user)
                .gas(100)
                .cash(500)
                .bugs(0)
                .coffee(50)
                .motivation(100)
                .locationIndex(0)
                .locationName("San Jose")
                .day(1)
                .status(GameStatus.IN_PROGRESS)
                .coffeeZeroStreak(0)
                .eventPending(false)
                .build();
    }
}

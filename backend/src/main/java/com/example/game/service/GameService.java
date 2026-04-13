package com.example.game.service;

import com.example.game.dto.response.GameStateResponse;
import com.example.game.dto.response.TurnResultResponse;
import com.example.game.entity.GameState;
import com.example.game.entity.User;
import com.example.game.gameLogic.action.ActionType;
import com.example.game.enums.GameStatus;
import com.example.game.exceptions.GameExistsException;
import com.example.game.exceptions.GameNotFoundException;
import com.example.game.exceptions.NotFoundException;
import com.example.game.exceptions.UserNotFoundException;
import com.example.game.gameLogic.GameEngine;
import com.example.game.gameLogic.records.TurnResult;
import com.example.game.gameLogic.records.EventOptionType;
import com.example.game.gameLogic.records.EventResult;
import com.example.game.gameLogic.location.LocationRegistry;
import com.example.game.gameLogic.service.EventService;
import com.example.game.gameLogic.service.WinLossService;
import com.example.game.repository.GameStateRepo;
import com.example.game.repository.UserRepo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@Getter
@Setter
@AllArgsConstructor
@Transactional
public class GameService {
    private final UserRepo userRepo;
    private final GameStateRepo gameStateRepo;
    private final GameEngine gameEngine;
    private final LocationRegistry locationRegistry;
    private final EventService eventService;
    private final WinLossService winLossService;
    private final ObjectMapper objectMapper;

    public GameStateResponse createGame(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));
        GameState gameState;
        if (gameStateRepo.existsByUserId(user.getId())) {
            boolean exists = gameStateRepo.existsByUserAndStatus(user, GameStatus.IN_PROGRESS);
            if(exists) {
                throw new GameExistsException("Game exists for this user");
            }
        }
        gameState = buildNewGame(user);
        gameStateRepo.save(gameState);
        return GameStateResponse.from(gameState, objectMapper);

    }
    @Transactional(readOnly = true)
    public GameStateResponse getGame(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        GameState gameState = gameStateRepo.findStateByUserAndStatus(user, GameStatus.IN_PROGRESS)
                .orElseThrow(() -> new GameNotFoundException("No saved game found"));

        System.out.println("loaded game id = " + gameState.getId());
        System.out.println("loaded pendingEventJson = " + gameState.getPendingEventJson());
        System.out.println("loaded eventPending = " + gameState.isEventPending());

        return GameStateResponse.from(gameState,objectMapper);
    }

    public GameState createOrOverrideGame(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));

        gameStateRepo.findStateByUserAndStatus(user,GameStatus.IN_PROGRESS).ifPresent(gameStateRepo::delete);
        GameState newGame = buildNewGame(user);
        GameState saved = gameStateRepo.save(newGame);
        return saved;
    }

    private GameState buildNewGame(User user) {
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
                .stateJson(null)
                .eventPending(false)
                .build();
    }

    public TurnResultResponse performAction(String username, ActionType actionType) {
        User user = userRepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));
        GameState gameState = gameStateRepo.findStateByUserAndStatus(user, GameStatus.IN_PROGRESS)
                .orElseThrow(() -> new NotFoundException("No active game found."));

        TurnResult turnResult = gameEngine.processTurn(gameState, actionType);

        System.out.println("before save pendingEventJson = " + turnResult.gameState().getPendingEventJson());
        System.out.println("before save eventPending = " + turnResult.gameState().isEventPending());

        GameState saved = gameStateRepo.save(turnResult.gameState());

        System.out.println("after save pendingEventJson = " + saved.getPendingEventJson());
        System.out.println("after save eventPending = " + saved.isEventPending());

        return TurnResultResponse.from(
                saved,
                locationRegistry.getByIndex(saved.getLocationIndex()).name(),
                turnResult.actionResult(),
                turnResult.pendingEvent()
        );
    }

    public void deleteGame(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));
        gameStateRepo.deleteByUserAndStatus(user, GameStatus.IN_PROGRESS);
    }

    public TurnResultResponse resolvePendingEvent(String username, EventOptionType choice) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        GameState gameState = gameStateRepo.findStateByUserAndStatus(user, GameStatus.IN_PROGRESS)
                .orElseThrow(() -> new NotFoundException("No active game found."));

        EventResult result = eventService.resolvePendingEvent(gameState, choice);

        winLossService.evaluate(gameState);

        GameState saved = gameStateRepo.save(gameState);

        return TurnResultResponse.fromResolvedEvent(
                saved,
                saved.getLocationName(),
                result
        );
    }

}

package com.example.game.service;

import com.example.game.dto.response.TurnResultResponse;
import com.example.game.entity.GameState;
import com.example.game.entity.User;
import com.example.game.enums.ActionType;
import com.example.game.enums.GameStatus;
import com.example.game.exceptions.GameExistsException;
import com.example.game.exceptions.GameNotFoundException;
import com.example.game.exceptions.NotFoundException;
import com.example.game.exceptions.UserNotFoundException;
import com.example.game.gameLogic.GameEngine;
import com.example.game.gameLogic.TurnResult;
import com.example.game.gameLogic.location.LocationRegistry;
import com.example.game.repository.GameStateRepo;
import com.example.game.repository.UserRepo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public GameState createGame(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));
        GameState gameState;
        if (gameStateRepo.existsByUserId(user.getId())) {
            gameState = gameStateRepo.findByUser(user).orElseThrow(()-> new GameExistsException("Game already exists"));
            if(gameState.getStatus().equals(GameStatus.IN_PROGRESS)) {
                throw new GameExistsException("Game exists for this user");
            }
        }
        gameState = buildNewGame(user);
        return gameStateRepo.save(gameState);
    }
    @Transactional(readOnly = true)
    public GameState getGame(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));
        GameState gameState = gameStateRepo.findByUser(user)
                .orElseThrow(() -> new GameNotFoundException("No saved game found"));
        return gameState;
    }

    public GameState createOrOverrideGame(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));

        gameStateRepo.findByUser(user).ifPresent(gameStateRepo::delete);
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
                .build();
    }

    public TurnResultResponse performAction(String username, ActionType actionType) {
        User user = userRepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));
        GameState gameState = gameStateRepo.findByUser(user)
                .orElseThrow(() -> new NotFoundException("No active game found."));

        TurnResult turnResult = gameEngine.processTurn(gameState, actionType);

        GameState saved = gameStateRepo.save(turnResult.gameState());

        return TurnResultResponse.from(
                saved,
                locationRegistry.getByIndex(saved.getLocationIndex()).name(),
                turnResult.actionResult()
        );
    }

    public void deleteGame(String username) {
        User user = userRepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));
        gameStateRepo.deleteByUser(user);
    }

}

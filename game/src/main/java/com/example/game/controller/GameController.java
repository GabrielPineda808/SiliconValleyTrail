package com.example.game.controller;

import com.example.game.dto.response.GameStateResponse;
import com.example.game.dto.response.TurnResultResponse;
import com.example.game.entity.GameState;
import com.example.game.enums.ActionType;
import com.example.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/game")
@RestController
@AllArgsConstructor
@Tag(name = "Game", description = "Performs game actions")
@SecurityRequirement(name = "bearerAuth")
public class GameController {
    private final GameService gameService;

    @PostMapping("/start")
    @Operation(summary = "start game", description = "Starts a game and returns the game State in response")
    public ResponseEntity<GameStateResponse> startGame(@AuthenticationPrincipal(expression = "username") String username) {
        GameState gameState = gameService.createGame(username);
        GameStateResponse gameStateResponse = GameStateResponse.from(gameState);
        return ResponseEntity.ok(gameStateResponse);
    }

    @GetMapping("/findGame")
    @Operation(summary = "get game", description = "Returns game state for requested game")
    public ResponseEntity<GameStateResponse> findGame(@AuthenticationPrincipal(expression = "username") String username) {
        GameState gameState = gameService.getGame(username);
        GameStateResponse gameStateResponse = GameStateResponse.from(gameState);
        return ResponseEntity.ok(gameStateResponse);
    }

    @PostMapping("/action")
    @Operation(summary = "action", description = "user performs action and affects game state")
    public ResponseEntity<TurnResultResponse> performAction(@NonNull @RequestBody ActionType action, @AuthenticationPrincipal(expression = "username") String username) {
        TurnResultResponse returnResponse = gameService.performAction(username, action);
        return ResponseEntity.ok(returnResponse);
    }

    @DeleteMapping("/delete")
    public void deleteGame(@AuthenticationPrincipal(expression = "username")String username) {
        gameService.deleteGame(username);
    }

    @PostMapping("/event/choice")
    public void eventChoice(@NonNull@RequestBody String choice,@AuthenticationPrincipal(expression = "username") String username) {
    }

}

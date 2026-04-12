package com.example.game.controller;

import com.example.game.dto.request.PerformActionRequest;
import com.example.game.dto.request.ResolveEventRequest;
import com.example.game.dto.response.GameStateResponse;
import com.example.game.dto.response.TurnResultResponse;
import com.example.game.exceptions.ErrorResponse;
import com.example.game.service.GameService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for managing the authenticated user's game session.
 */
@RequestMapping("/game")
@RestController
@AllArgsConstructor
@Tag(name = "Game", description = "Performs game actions")
@SecurityRequirement(name = "bearerAuth")
public class GameController {
    private final GameService gameService;

    /**
     * Starts a new game for the authenticated user.
     *
     * @param username authenticated username extracted from the JWT
     * @return initial game state
     */
    @PostMapping("/start")
    @Operation(summary = "Start Game", description = "Creates a new game for the authenticated user and returns the initial state.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Game started successfully."),
            @ApiResponse(responseCode = "404", description = "User not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "An active game already exists.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GameStateResponse> startGame(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "username") String username
    ) {
        return ResponseEntity.ok(gameService.createGame(username));
    }

    /**
     * Returns the active game state for the authenticated user.
     *
     * @param username authenticated username extracted from the JWT
     * @return current in-progress game state
     */
    @GetMapping("/findGame")
    @Operation(summary = "Get Active Game", description = "Returns the active game state for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active game returned successfully."),
            @ApiResponse(responseCode = "404", description = "No active game or user was found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GameStateResponse> findGame(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "username") String username
    ) {
        return ResponseEntity.ok(gameService.getGame(username));
    }

    /**
     * Performs a turn action for the authenticated user's active game.
     *
     * @param action requested turn action
     * @param username authenticated username extracted from the JWT
     * @return updated turn result after the action is processed
     */
    @PostMapping("/action")
    @Operation(summary = "Perform Action", description = "Executes a turn action and returns the resulting game state changes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Action completed successfully."),
            @ApiResponse(responseCode = "400", description = "The action request was invalid.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No active game or user was found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TurnResultResponse> performAction(
            @NonNull @RequestBody PerformActionRequest action,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "username") String username
    ) {
        return ResponseEntity.ok(gameService.performAction(username, action.action()));
    }

    /**
     * Deletes the authenticated user's active game, if one exists.
     *
     * @param username authenticated username extracted from the JWT
     */
    @DeleteMapping("/delete")
    @Operation(summary = "Delete Active Game", description = "Deletes the authenticated user's current in-progress game.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active game deleted or no active game existed."),
            @ApiResponse(responseCode = "404", description = "User not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void deleteGame(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "username") String username
    ) {
        gameService.deleteGame(username);
    }

    /**
     * Resolves the currently pending event for the authenticated user.
     *
     * @param request selected event choice
     * @param username authenticated username extracted from the JWT
     * @return updated turn result after the event is resolved
     */
    @PostMapping("/event/choice")
    @Operation(summary = "Resolve Pending Event", description = "Applies the selected choice for the current pending event.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event resolved successfully."),
            @ApiResponse(responseCode = "400", description = "The event choice was invalid.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No active game or user was found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TurnResultResponse> eventChoice(
            @NonNull @RequestBody ResolveEventRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "username") String username
    ) {
        return ResponseEntity.ok(
                gameService.resolvePendingEvent(username, request.choice())
        );
    }

}

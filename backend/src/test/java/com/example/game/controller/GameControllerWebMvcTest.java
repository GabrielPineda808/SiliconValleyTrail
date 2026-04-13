package com.example.game.controller;

import com.example.game.dto.response.GameStateResponse;
import com.example.game.dto.response.PendingEventResponse;
import com.example.game.dto.response.TurnResultResponse;
import com.example.game.gameLogic.action.ActionType;
import com.example.game.enums.EventType;
import com.example.game.enums.GameStatus;
import com.example.game.exceptions.GameExistsException;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.exceptions.NotFoundException;
import com.example.game.gameLogic.records.EventOption;
import com.example.game.gameLogic.records.EventOptionType;
import com.example.game.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class GameControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private GameService gameService;

    @Test
    @WithMockUser(username = "gabe")
    void startGameReturnsInitialStateForAuthenticatedUser() throws Exception {
        when(gameService.createGame("gabe")).thenReturn(TestData.gameState());

        mockMvc.perform(post("/game/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(5))
                .andExpect(jsonPath("$.locationName").value("San Jose"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(gameService).createGame("gabe");
    }

    @Test
    @WithMockUser(username = "gabe")
    void startGameUsesConflictErrorWhenActiveGameAlreadyExists() throws Exception {
        when(gameService.createGame("gabe")).thenThrow(new GameExistsException("Game exists for this user"));

        mockMvc.perform(post("/game/start"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("GAME_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Game exists for this user"))
                .andExpect(jsonPath("$.path").value("/game/start"));
    }

    @Test
    @WithMockUser(username = "gabe")
    void findGameReturnsCurrentStateForAuthenticatedUser() throws Exception {
        when(gameService.getGame("gabe")).thenReturn(TestData.gameState());

        mockMvc.perform(get("/game/findGame"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(5))
                .andExpect(jsonPath("$.locationName").value("San Jose"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = "gabe")
    void performActionReturnsTurnResultWithPendingEvent() throws Exception {
        PendingEventResponse pendingEvent = new PendingEventResponse(
                EventType.VC_FUNDING_OFFER,
                "Pitch deck",
                "A VC reaches out",
                List.of(new EventOption(EventOptionType.ACCEPT, "Accept", "Take the money"))
        );
        TurnResultResponse response = new TurnResultResponse(
                5L,
                90,
                650,
                1,
                45,
                80,
                1,
                "San Francisco",
                4,
                GameStatus.IN_PROGRESS,
                "You traveled",
                List.of("-10 gas"),
                pendingEvent
        );
        when(gameService.performAction("gabe", ActionType.TRAVEL)).thenReturn(response);

        mockMvc.perform(post("/game/action")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ActionPayload(ActionType.TRAVEL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You traveled"))
                .andExpect(jsonPath("$.pendingEvent.type").value("VC_FUNDING_OFFER"))
                .andExpect(jsonPath("$.pendingEvent.choices[0].optionType").value("ACCEPT"))
                .andExpect(jsonPath("$.pendingEvent.choices[0].label").value("Accept"));

        verify(gameService).performAction("gabe", ActionType.TRAVEL);
    }

    @Test
    @WithMockUser(username = "gabe")
    void performActionUsesErrorHandlerForInvalidGameAction() throws Exception {
        when(gameService.performAction("gabe", ActionType.TRAVEL))
                .thenThrow(new InvalidActionException("Resolve the current event before taking another action"));

        mockMvc.perform(post("/game/action")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ActionPayload(ActionType.TRAVEL))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ACTION"))
                .andExpect(jsonPath("$.message").value("Resolve the current event before taking another action"))
                .andExpect(jsonPath("$.path").value("/game/action"));
    }

    @Test
    @WithMockUser(username = "gabe")
    void performActionRejectsInvalidEnumPayload() throws Exception {
        mockMvc.perform(post("/game/action")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "INVALID_ACTION"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "gabe")
    void performActionRejectsMissingBody() throws Exception {
        mockMvc.perform(post("/game/action")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "gabe")
    void resolveEventChoiceReturnsResolvedTurnResult() throws Exception {
        TurnResultResponse response = new TurnResultResponse(
                5L,
                90,
                700,
                1,
                45,
                85,
                1,
                "San Francisco",
                4,
                GameStatus.IN_PROGRESS,
                "You took the deal",
                List.of("+200 cash"),
                null
        );
        when(gameService.resolvePendingEvent("gabe", EventOptionType.ACCEPT)).thenReturn(response);

        mockMvc.perform(post("/game/event/choice")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EventChoicePayload(EventOptionType.ACCEPT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You took the deal"))
                .andExpect(jsonPath("$.pendingEvent").doesNotExist());

        verify(gameService).resolvePendingEvent("gabe", EventOptionType.ACCEPT);
    }

    @Test
    @WithMockUser(username = "gabe")
    void resolveEventChoiceUsesErrorHandlerForInvalidChoice() throws Exception {
        when(gameService.resolvePendingEvent("gabe", EventOptionType.ACCEPT))
                .thenThrow(new InvalidActionException("Invalid option for pending event: ACCEPT"));

        mockMvc.perform(post("/game/event/choice")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EventChoicePayload(EventOptionType.ACCEPT))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ACTION"))
                .andExpect(jsonPath("$.message").value("Invalid option for pending event: ACCEPT"))
                .andExpect(jsonPath("$.path").value("/game/event/choice"));
    }

    @Test
    @WithMockUser(username = "gabe")
    void resolveEventChoiceRejectsInvalidEnumPayload() throws Exception {
        mockMvc.perform(post("/game/event/choice")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "choice": "NOT_A_REAL_CHOICE"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "gabe")
    void deleteGameReturnsOkAndDelegatesToService() throws Exception {
        mockMvc.perform(delete("/game/delete"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(gameService).deleteGame("gabe");
    }

    @Test
    @WithMockUser(username = "gabe")
    void findGameUsesGlobalExceptionHandlerForMissingState() throws Exception {
        when(gameService.getGame("gabe")).thenThrow(new NotFoundException("No active game found."));

        mockMvc.perform(get("/game/findGame"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No active game found."))
                .andExpect(jsonPath("$.path").value("/game/findGame"));
    }

    private record ActionPayload(ActionType action) {
    }

    private record EventChoicePayload(EventOptionType choice) {
    }

    private static final class TestData {
        private static GameStateResponse gameState() {
            return new GameStateResponse(
                    5L,
                    100,
                    500,
                    0,
                    50,
                    100,
                    0,
                    "San Jose",
                    1,
                    GameStatus.IN_PROGRESS,
                    null
            );
        }
    }
}

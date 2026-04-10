package com.example.game.controller;

import com.example.game.dto.response.PendingEventResponse;
import com.example.game.dto.response.TurnResultResponse;
import com.example.game.enums.ActionType;
import com.example.game.enums.EventType;
import com.example.game.enums.GameStatus;
import com.example.game.exceptions.NotFoundException;
import com.example.game.gameLogic.event.records.EventChoice;
import com.example.game.gameLogic.event.records.EventOptionType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                List.of(new EventChoice("Accept", EventOptionType.ACCEPT))
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
                .andExpect(jsonPath("$.pendingEvent.choices[0].choice").value("ACCEPT"));

        verify(gameService).performAction("gabe", ActionType.TRAVEL);
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

    private static final class TestData {
        private static com.example.game.entity.GameState gameState() {
            return com.example.game.entity.GameState.builder()
                    .id(5L)
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
}

package com.example.game.dto.response;

import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.enums.GameStatus;
import com.example.game.gameLogic.records.EventOption;
import com.example.game.gameLogic.records.EventOptionType;
import com.example.game.gameLogic.records.PendingEvent;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameStateResponseTest {

    private final ObjectMapper objectMapper = mock(ObjectMapper.class);

    @Test
    void fromMapsPendingEventJsonIntoResponsePayload() throws Exception {
        GameState gameState = baseState();
        PendingEvent pendingEvent = new PendingEvent(
                EventType.VC_FUNDING_OFFER,
                "VC Funding Offer",
                "Pitch to investors",
                List.of(new EventOption(EventOptionType.ACCEPT, "Take meeting", "Risk motivation for cash"))
        );
        gameState.setPendingEventJson("{\"type\":\"VC_FUNDING_OFFER\"}");
        when(objectMapper.readValue(gameState.getPendingEventJson(), PendingEvent.class)).thenReturn(pendingEvent);

        GameStateResponse response = GameStateResponse.from(gameState, objectMapper);

        assertThat(response.gameId()).isEqualTo(12L);
        assertThat(response.locationName()).isEqualTo("San Jose");
        assertThat(response.pendingEvent()).isNotNull();
        assertThat(response.pendingEvent().type()).isEqualTo(EventType.VC_FUNDING_OFFER);
        assertThat(response.pendingEvent().choices()).extracting(EventOption::optionType)
                .containsExactly(EventOptionType.ACCEPT);
    }

    @Test
    void fromThrowsWhenPendingEventJsonIsInvalid() throws Exception {
        GameState gameState = baseState();
        gameState.setPendingEventJson("bad-json");
        when(objectMapper.readValue("bad-json", PendingEvent.class)).thenThrow(new RuntimeException("broken"));

        assertThatThrownBy(() -> GameStateResponse.from(gameState, objectMapper))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid pending event JSON");
    }

    private static GameState baseState() {
        return GameState.builder()
                .id(12L)
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

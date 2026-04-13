package com.example.game.gameLogic.event;

import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.enums.GameStatus;
import com.example.game.gameLogic.event.flightAPI.AirTrafficData;
import com.example.game.gameLogic.event.flightAPI.AirTrafficProvider;
import com.example.game.gameLogic.location.GameLocation;
import com.example.game.gameLogic.location.LocationRegistry;
import com.example.game.gameLogic.records.EventOptionType;
import com.example.game.gameLogic.records.EventResult;
import com.example.game.gameLogic.records.PendingEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirTrafficEventTest {

    @Mock
    private AirTrafficProvider airTrafficProvider;

    @Mock
    private LocationRegistry locationRegistry;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void createPendingEventIncludesFallbackMetadataAndChoices() {
        AirTrafficEvent event = new AirTrafficEvent(airTrafficProvider, locationRegistry, objectMapper);
        GameState gameState = baseState();
        GameLocation location = new GameLocation("San Francisco", 1, 1);
        when(locationRegistry.getByIndex(0)).thenReturn(location);
        when(airTrafficProvider.getAirTraffic(location)).thenReturn(new AirTrafficData(24, true));

        PendingEvent pendingEvent = event.createPendingEvent(gameState);

        assertThat(pendingEvent.type()).isEqualTo(EventType.AIR_TRAFFIC_BUZZ);
        assertThat(pendingEvent.description()).contains("Fallback traffic data was used.");
        assertThat(pendingEvent.description()).contains("[traffic_count=24][fallback=true]");
        assertThat(pendingEvent.choices()).extracting(choice -> choice.optionType())
                .containsExactly(EventOptionType.PUSH_FORWARD, EventOptionType.REST);
    }

    @Test
    void resolvePushForwardUsesAircraftCountParsedFromDescription() throws Exception {
        AirTrafficEvent event = new AirTrafficEvent(airTrafficProvider, locationRegistry, objectMapper);
        GameState gameState = baseState();
        gameState.setPendingEventJson("stored");
        PendingEvent pendingEvent = new PendingEvent(
                EventType.AIR_TRAFFIC_BUZZ,
                "Air Traffic Buzz",
                "The airspace near San Francisco is packed. Around 24 aircraft are nearby, and the whole region feels intense. Fallback traffic data was used. [traffic_count=24][fallback=true]",
                java.util.List.of()
        );
        when(objectMapper.readValue("stored", PendingEvent.class)).thenReturn(pendingEvent);

        EventResult result = event.resolve(gameState, EventOptionType.PUSH_FORWARD);

        assertThat(gameState.getMotivation()).isEqualTo(76);
        assertThat(gameState.getBugs()).isEqualTo(4);
        assertThat(result.effects()).containsExactly("-4 motivation", "+2 bugs");
    }

    @Test
    void resolveRestFailsWhenDescriptionCannotBeParsed() throws Exception {
        AirTrafficEvent event = new AirTrafficEvent(airTrafficProvider, locationRegistry, objectMapper);
        GameState gameState = baseState();
        gameState.setPendingEventJson("stored");
        PendingEvent pendingEvent = new PendingEvent(
                EventType.AIR_TRAFFIC_BUZZ,
                "Air Traffic Buzz",
                "No aircraft count appears here.",
                java.util.List.of()
        );
        when(objectMapper.readValue("stored", PendingEvent.class)).thenReturn(pendingEvent);

        assertThatThrownBy(() -> event.resolve(gameState, EventOptionType.REST))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to read air traffic event data");
    }

    private static GameState baseState() {
        return GameState.builder()
                .gas(100)
                .cash(500)
                .bugs(2)
                .coffee(10)
                .motivation(80)
                .locationIndex(0)
                .locationName("San Jose")
                .day(1)
                .status(GameStatus.IN_PROGRESS)
                .coffeeZeroStreak(0)
                .eventPending(true)
                .build();
    }
}

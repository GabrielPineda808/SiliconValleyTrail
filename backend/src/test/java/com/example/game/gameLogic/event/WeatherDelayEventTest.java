package com.example.game.gameLogic.event;

import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.enums.GameStatus;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.event.weatherAPI.WeatherData;
import com.example.game.gameLogic.event.weatherAPI.WeatherProvider;
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
class WeatherDelayEventTest {

    @Mock
    private WeatherProvider weatherProvider;

    @Mock
    private LocationRegistry locationRegistry;

    @Mock
    private RandomProvider randomProvider;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void createPendingEventIncludesFallbackSourceTextAndWeatherSpecificChoices() {
        WeatherDelayEvent event = new WeatherDelayEvent(weatherProvider, locationRegistry, randomProvider, objectMapper);
        GameState gameState = baseState();
        GameLocation location = new GameLocation("San Francisco", 1, 1);
        when(locationRegistry.getByIndex(0)).thenReturn(location);
        when(weatherProvider.getWeather(location)).thenReturn(new WeatherData(58, 45, true));

        PendingEvent pendingEvent = event.createPendingEvent(gameState);

        assertThat(pendingEvent.type()).isEqualTo(EventType.WEATHER_SHIFT);
        assertThat(pendingEvent.description()).contains("Fallback weather data was used.");
        assertThat(pendingEvent.description()).contains("58.0F");
        assertThat(pendingEvent.choices()).extracting(choice -> choice.optionType())
                .containsExactly(EventOptionType.PUSH_FORWARD, EventOptionType.REST);
    }

    @Test
    void resolvePushForwardUsesThunderDescriptionToApplyStormPenalties() throws Exception {
        WeatherDelayEvent event = new WeatherDelayEvent(weatherProvider, locationRegistry, randomProvider, objectMapper);
        GameState gameState = baseState();
        gameState.setPendingEventJson("stored");
        PendingEvent pendingEvent = new PendingEvent(
                EventType.WEATHER_SHIFT,
                "Weather Shift",
                "Thunderstorms are rolling through San Francisco at 58.0F. This could wreck the day. Fallback weather data was used.",
                java.util.List.of()
        );
        when(objectMapper.readValue("stored", PendingEvent.class)).thenReturn(pendingEvent);
        when(randomProvider.nextIntInclusive(2, 5)).thenReturn(4);
        when(randomProvider.nextIntInclusive(4, 7)).thenReturn(6);

        EventResult result = event.resolve(gameState, EventOptionType.PUSH_FORWARD);

        assertThat(gameState.getBugs()).isEqualTo(6);
        assertThat(gameState.getMotivation()).isEqualTo(74);
        assertThat(result.effects()).containsExactly("+4 bugs", "-6 motivation");
    }

    @Test
    void resolveRestUsesDrizzleDescriptionToApplyCafeRecovery() throws Exception {
        WeatherDelayEvent event = new WeatherDelayEvent(weatherProvider, locationRegistry, randomProvider, objectMapper);
        GameState gameState = baseState();
        gameState.setPendingEventJson("stored");
        PendingEvent pendingEvent = new PendingEvent(
                EventType.WEATHER_SHIFT,
                "Weather Shift",
                "A light drizzle falls in San Francisco at 61.0F. The team can work through it, but morale dips. Fallback weather data was used.",
                java.util.List.of()
        );
        when(objectMapper.readValue("stored", PendingEvent.class)).thenReturn(pendingEvent);
        when(randomProvider.nextIntInclusive(10, 20)).thenReturn(14);
        when(randomProvider.nextIntInclusive(1, 2)).thenReturn(2);

        EventResult result = event.resolve(gameState, EventOptionType.REST);

        assertThat(gameState.getCash()).isEqualTo(486);
        assertThat(gameState.getCoffee()).isEqualTo(12);
        assertThat(result.effects()).containsExactly("-14 cash", "+2 coffee");
    }

    @Test
    void resolveFailsWhenWeatherDescriptionCannotBeParsed() throws Exception {
        WeatherDelayEvent event = new WeatherDelayEvent(weatherProvider, locationRegistry, randomProvider, objectMapper);
        GameState gameState = baseState();
        gameState.setPendingEventJson("stored");
        PendingEvent pendingEvent = new PendingEvent(
                EventType.WEATHER_SHIFT,
                "Weather Shift",
                "Conditions are mysterious today.",
                java.util.List.of()
        );
        when(objectMapper.readValue("stored", PendingEvent.class)).thenReturn(pendingEvent);

        assertThatThrownBy(() -> event.resolve(gameState, EventOptionType.PUSH_FORWARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to read weather event data");
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

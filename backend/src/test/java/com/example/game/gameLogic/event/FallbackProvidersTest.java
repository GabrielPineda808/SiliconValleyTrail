package com.example.game.gameLogic.event;

import com.example.game.gameLogic.event.flightAPI.AirTrafficData;
import com.example.game.gameLogic.event.flightAPI.FallbackAirTrafficProvider;
import com.example.game.gameLogic.event.flightAPI.OpenSkyProvider;
import com.example.game.gameLogic.event.weatherAPI.FallbackWeatherProvider;
import com.example.game.gameLogic.event.weatherAPI.OpenMeteoWeatherProvider;
import com.example.game.gameLogic.event.weatherAPI.WeatherData;
import com.example.game.gameLogic.location.GameLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FallbackProvidersTest {

    @Mock
    private OpenMeteoWeatherProvider openMeteoWeatherProvider;

    @Mock
    private OpenSkyProvider openSkyProvider;

    @Test
    void fallbackWeatherProviderReturnsPrimaryValueWhenAvailable() {
        FallbackWeatherProvider provider = new FallbackWeatherProvider(openMeteoWeatherProvider);
        GameLocation location = new GameLocation("San Jose", 0, 0);
        WeatherData live = new WeatherData(68, 2, false);
        when(openMeteoWeatherProvider.getWeather(location)).thenReturn(live);

        assertThat(provider.getWeather(location)).isEqualTo(live);
    }

    @Test
    void fallbackWeatherProviderReturnsLocationSpecificMockWhenPrimaryFails() {
        FallbackWeatherProvider provider = new FallbackWeatherProvider(openMeteoWeatherProvider);
        GameLocation location = new GameLocation("San Francisco", 0, 0);
        when(openMeteoWeatherProvider.getWeather(location)).thenThrow(new RuntimeException("offline"));

        WeatherData fallback = provider.getWeather(location);

        assertThat(fallback).isEqualTo(new WeatherData(58, 45, true));
    }

    @Test
    void fallbackAirTrafficProviderReturnsPrimaryValueWhenAvailable() {
        FallbackAirTrafficProvider provider = new FallbackAirTrafficProvider(openSkyProvider);
        GameLocation location = new GameLocation("Palo Alto", 0, 0);
        AirTrafficData live = new AirTrafficData(13, false);
        when(openSkyProvider.getAirTraffic(location)).thenReturn(live);

        assertThat(provider.getAirTraffic(location)).isEqualTo(live);
    }

    @Test
    void fallbackAirTrafficProviderReturnsLocationSpecificMockWhenPrimaryFails() {
        FallbackAirTrafficProvider provider = new FallbackAirTrafficProvider(openSkyProvider);
        GameLocation location = new GameLocation("San Francisco", 0, 0);
        when(openSkyProvider.getAirTraffic(location)).thenThrow(new RuntimeException("offline"));

        AirTrafficData fallback = provider.getAirTraffic(location);

        assertThat(fallback).isEqualTo(new AirTrafficData(26, true));
    }
}

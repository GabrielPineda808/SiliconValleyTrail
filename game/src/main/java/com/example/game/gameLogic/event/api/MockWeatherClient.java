package com.example.game.gameLogic.event.api;

import com.example.game.gameLogic.RandomProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class MockWeatherClient implements WeatherClient {

    private final OpenMeteoWeatherClient openMeteoWeatherClient;
    private final RandomProvider randomProvider;

    @Override
    public boolean hasTravelDelayRisk(double latitude, double longitude) {
        try {
            return openMeteoWeatherClient.hasTravelDelayRisk(latitude, longitude);
        } catch (Exception ex) {
            return randomProvider.chance(0.10);
        }
    }
}

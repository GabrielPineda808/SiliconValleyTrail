package com.example.game.gameLogic.event.api;

import com.example.game.gameLogic.RandomProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
public class MockFlightClient implements FlightActivityClient {

    private final OpenSkyFlightActivityClient openSkyFlightActivityClient;
    private final RandomProvider randomProvider;

    @Override
    public boolean hasNearbyFlightActivity(double latitude, double longitude) {
        try {
            return openSkyFlightActivityClient.hasNearbyFlightActivity(latitude, longitude);
        } catch (Exception ex) {
            return randomProvider.chance(0.08);
        }
    }
}

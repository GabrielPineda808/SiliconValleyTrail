package com.example.game.gameLogic.action.travel;

import com.example.game.gameLogic.RandomProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GasCalc {
    private static final double ROAD_MULTIPLIER = 1.20;
    private static final double KM_PER_GAS_UNIT = 5.0;
    private final RandomProvider randomProvider;

    public int calculateGasCost(double fromLat, double fromLon, double toLat, double toLon) {
        double straightLineKm = DistanceCalc.haversineKm(fromLat, fromLon, toLat, toLon);
        double effectiveDistanceKm = straightLineKm * ROAD_MULTIPLIER;

        double variance = randomProvider.nextDouble(0.95,1.11);
        double adjustedDistanceKm = effectiveDistanceKm * variance;

        return Math.max(1, (int) Math.ceil(adjustedDistanceKm / KM_PER_GAS_UNIT));
    }
}

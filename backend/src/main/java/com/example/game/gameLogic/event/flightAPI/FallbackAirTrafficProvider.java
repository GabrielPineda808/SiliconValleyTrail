package com.example.game.gameLogic.event.flightAPI;


import com.example.game.gameLogic.location.GameLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class FallbackAirTrafficProvider implements AirTrafficProvider {

    private final OpenSkyProvider primary;


    @Override
    public AirTrafficData getAirTraffic(GameLocation location) {
        try {
            return primary.getAirTraffic(location);
        } catch (Exception e) {
            return mockFor(location);
        }
    }

    private AirTrafficData mockFor(GameLocation location) {
        return switch (location.name()) {
            case "San Jose" -> new AirTrafficData(18, true);
            case "Santa Clara" -> new AirTrafficData(14, true);
            case "Sunnyvale" -> new AirTrafficData(12, true);
            case "Mountain View" -> new AirTrafficData(10, true);
            case "Palo Alto" -> new AirTrafficData(9, true);
            case "Menlo Park" -> new AirTrafficData(8, true);
            case "Redwood City" -> new AirTrafficData(11, true);
            case "San Mateo" -> new AirTrafficData(16, true);
            case "Burlingame" -> new AirTrafficData(20, true);
            case "South San Francisco" -> new AirTrafficData(22, true);
            case "San Francisco" -> new AirTrafficData(26, true);
            default -> new AirTrafficData(12, true);
        };
    }
}

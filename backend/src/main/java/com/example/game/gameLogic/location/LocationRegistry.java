package com.example.game.gameLogic.location;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class LocationRegistry {
    private static final List<GameLocation> LOCATIONS = List.of(
            new GameLocation( "San Jose", 37.3382, -121.8863),
            new GameLocation( "Santa Clara", 37.3541, -121.9552),
            new GameLocation( "Sunnyvale", 37.3688, -122.0363),
            new GameLocation( "Mountain View", 37.3861, -122.0839),
            new GameLocation( "Palo Alto", 37.4419, -122.1430),
            new GameLocation( "Menlo Park", 37.4530, -122.1817),
            new GameLocation( "Redwood City", 37.4852, -122.2364),
            new GameLocation( "San Mateo", 37.5630, -122.3255),
            new GameLocation( "Burlingame", 37.5779, -122.3481),
            new GameLocation("South San Francisco", 37.6547, -122.4077),
            new GameLocation( "San Francisco", 37.7749, -122.4194)
    );

    public GameLocation getByIndex(int index) {
        return LOCATIONS.get(index);
    }

    public int lastIndex() {
        return LOCATIONS.size() - 1;
    }
}

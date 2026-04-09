package com.example.game.gameLogic.location;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class LocationRegistry {
    private static final List<GameLocation> LOCATIONS = List.of(
            new GameLocation(0, "San Jose", 37.3382, -121.8863),
            new GameLocation(1, "Santa Clara", 37.3541, -121.9552),
            new GameLocation(2, "Sunnyvale", 37.3688, -122.0363),
            new GameLocation(3, "Mountain View", 37.3861, -122.0839),
            new GameLocation(4, "Palo Alto", 37.4419, -122.1430),
            new GameLocation(5, "Menlo Park", 37.4530, -122.1817),
            new GameLocation(6, "Redwood City", 37.4852, -122.2364),
            new GameLocation(7, "San Mateo", 37.5630, -122.3255),
            new GameLocation(8, "Burlingame", 37.5779, -122.3481),
            new GameLocation(9, "South San Francisco", 37.6547, -122.4077),
            new GameLocation(10, "San Francisco", 37.7749, -122.4194)
    );

    public GameLocation getByIndex(int index) {
        return LOCATIONS.get(index);
    }

    public int lastIndex() {
        return LOCATIONS.size() - 1;
    }
}

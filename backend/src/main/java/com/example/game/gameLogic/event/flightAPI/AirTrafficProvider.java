package com.example.game.gameLogic.event.flightAPI;

import com.example.game.gameLogic.location.GameLocation;

public interface AirTrafficProvider {
    AirTrafficData getAirTraffic(GameLocation location);
}

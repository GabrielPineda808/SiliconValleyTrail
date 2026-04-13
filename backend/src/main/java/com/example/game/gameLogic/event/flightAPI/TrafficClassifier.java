package com.example.game.gameLogic.event.flightAPI;


public class TrafficClassifier {

    public static Traffic classify(int nearbyAircraftCount) {
        if (nearbyAircraftCount <= 6) return Traffic.QUIET;
        if (nearbyAircraftCount <= 14) return Traffic.MODERATE;
        if (nearbyAircraftCount <= 22) return Traffic.BUSY;
        return Traffic.JAMMED;
    }
}
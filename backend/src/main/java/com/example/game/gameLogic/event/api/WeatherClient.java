package com.example.game.gameLogic.event.api;

public interface WeatherClient {
    boolean hasTravelDelayRisk(double latitude, double longitude);
}

package com.example.game.gameLogic.event.weatherAPI;

import com.example.game.gameLogic.location.GameLocation;

public interface WeatherProvider {
    WeatherData getWeather(GameLocation location);
}


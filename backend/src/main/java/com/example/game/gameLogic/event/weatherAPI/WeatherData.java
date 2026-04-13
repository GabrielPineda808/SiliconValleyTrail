package com.example.game.gameLogic.event.weatherAPI;

public record WeatherData(
        double temperature,
        int weatherCode,
        boolean fallback
) {}

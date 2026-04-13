package com.example.game.gameLogic.event.weatherAPI;

public class WeatherClassifier {

    public Weather classify(int weatherCode) {
        return switch (weatherCode) {
            case 0, 1 -> Weather.CLEAR;
            case 2, 3 -> Weather.CLOUDY;
            case 45, 48 -> Weather.FOGGY;
            case 51, 53, 55, 56, 57 -> Weather.DRIZZLE;
            case 61, 63, 65, 66, 67, 80, 81, 82 -> Weather.RAIN;
            case 95, 96, 99 -> Weather.THUNDER;
            default -> Weather.CLOUDY;
        };
    }
}

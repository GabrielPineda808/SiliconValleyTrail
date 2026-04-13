package com.example.game.gameLogic.event.weatherAPI;

import com.example.game.gameLogic.location.GameLocation;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Primary
public class FallbackWeatherProvider implements WeatherProvider{

    private final OpenMeteoWeatherProvider primary;

    @Override
    public WeatherData getWeather(GameLocation location) {
        try {
            return primary.getWeather(location);
        } catch (Exception e) {
            return mockFor(location);
        }
    }

    private WeatherData mockFor(GameLocation location) {
        return switch (location.name()) {
            case "San Jose" -> new WeatherData(78, 0, true);
            case "Santa Clara" -> new WeatherData(76, 1, true);
            case "Sunnyvale" -> new WeatherData(75, 0, true);
            case "Mountain View" -> new WeatherData(73, 2, true);
            case "Palo Alto" -> new WeatherData(74, 1, true);
            case "Menlo Park" -> new WeatherData(72, 2, true);
            case "Redwood City" -> new WeatherData(70, 1, true);
            case "San Mateo" -> new WeatherData(66, 3, true);
            case "Burlingame" -> new WeatherData(63, 45, true);
            case "South San Francisco" -> new WeatherData(61, 3, true);
            case "San Francisco" -> new WeatherData(58, 45, true);
            default -> new WeatherData(68, 2, true);
        };
    }

}

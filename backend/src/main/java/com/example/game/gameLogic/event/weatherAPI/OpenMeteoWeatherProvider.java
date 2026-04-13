package com.example.game.gameLogic.event.weatherAPI;

import com.example.game.gameLogic.location.GameLocation;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenMeteoWeatherProvider implements WeatherProvider {
    private final RestClient restClient;

    public OpenMeteoWeatherProvider(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://api.open-meteo.com")
                .build();
    }


    //payload
//    {
//        "latitude": 35.7,
//            "longitude": 139.6875,
//            "generationtime_ms": 0.038,
//            "utc_offset_seconds": 0,
//            "timezone": "GMT",
//            "timezone_abbreviation": "GMT",
//            "elevation": 40.0,
//            "current_units": {
//        "time": "iso8601",
//                "interval": "seconds",
//                "temperature_2m": "°C",
//                "weather_code": "wmo code"
//    },
//        "current": {
//        "time": "2026-04-13T03:30",
//                "interval": 900,
//                "temperature_2m": 21.6,
//                "weather_code": 1
//    }
//    }

    @Override
    public WeatherData getWeather(GameLocation location) {
        OpenMeteoResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", location.latitude())
                        .queryParam("longitude", location.longitude())
                        .queryParam("current", "temperature_2m,weather_code")
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);

        if (response == null) {
            throw new IllegalStateException("Open-Meteo returned no current weather");
        }

        return new WeatherData(
                response.temperature_2m(),
                response.weather_code(),
                false
        );
    }

    public record OpenMeteoResponse(double temperature_2m, int weather_code) {}
}

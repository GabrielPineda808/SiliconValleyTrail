package com.example.game.gameLogic.event.api;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class OpenMeteoWeatherClient implements WeatherClient{
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.open-meteo.com/v1")
            .build();

    @Override
    public boolean hasTravelDelayRisk(double latitude, double longitude) {


            Map<String, Object> body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("hourly", "precipitation_probability,precipitation,weather_code,wind_speed_10m")
                            .queryParam("forecast_days", 1)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (body == null) {
                return false;
            }

            Map<String, Object> hourly = (Map<String, Object>) body.get("hourly");
            if (hourly == null) {
                return false;
            }

            List<Number> precipitationProb = (List<Number>) hourly.get("precipitation_probability");
            if (precipitationProb == null || precipitationProb.isEmpty()) {
                return false;
            }

            return precipitationProb.get(0).intValue() >= 50;
        }
}

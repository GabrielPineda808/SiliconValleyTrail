package com.example.game.gameLogic.event.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenSkyFlightActivityClient implements FlightActivityClient {

    private final RestTemplateBuilder restTemplateBuilder;

    private String baseUrl="https://opensky-network.org/api/states/all";

    @Override
    public boolean hasNearbyFlightActivity(double latitude, double longitude) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        double latDelta = 0.20;
        double lonDelta = 0.20;

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("lamin", latitude - latDelta)
                .queryParam("lamax", latitude + latDelta)
                .queryParam("lomin", longitude - lonDelta)
                .queryParam("lomax", longitude + lonDelta)
                .toUriString();

        Map<String, Object> body = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();

        if (body == null) {
            return false;
        }

        List<?> states = (List<?>) body.get("states");
        return states != null && states.size() >= 8;
    }
}

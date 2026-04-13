package com.example.game.gameLogic.event.flightAPI;

import com.example.game.gameLogic.location.GameLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OpenSkyProvider implements AirTrafficProvider {

    private static final double BOX_OFFSET = 0.18;

    private final RestClient restClient;

    public OpenSkyProvider(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("https://opensky-network.org/api")
                .build();
    }

    @Override
    public AirTrafficData getAirTraffic(GameLocation location) {
        OpenSkyStatesResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/states/all")
                        .queryParam("lamin", location.latitude() - BOX_OFFSET)
                        .queryParam("lomin", location.longitude() - BOX_OFFSET)
                        .queryParam("lamax", location.latitude() + BOX_OFFSET)
                        .queryParam("lomax", location.longitude() + BOX_OFFSET)
                        .build())
                .retrieve()
                .body(OpenSkyStatesResponse.class);

        if (response == null || response.states() == null) {
            throw new IllegalStateException("OpenSky returned no state data");
        }

        int aircraftCount = (int) response.states().stream()
                .filter(state -> state != null && !state.isEmpty() && state.getFirst() != null)
                .count();

        return new AirTrafficData(aircraftCount, false);
    }

    public record OpenSkyStatesResponse(
            long time,
            List<List<Object>> states
    ) {}
}

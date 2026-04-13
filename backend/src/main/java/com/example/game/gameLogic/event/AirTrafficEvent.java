package com.example.game.gameLogic.event;

import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.gameLogic.event.flightAPI.AirTrafficData;
import com.example.game.gameLogic.event.flightAPI.AirTrafficProvider;
import com.example.game.gameLogic.event.flightAPI.Traffic;
import com.example.game.gameLogic.event.flightAPI.TrafficClassifier;
import com.example.game.gameLogic.location.GameLocation;
import com.example.game.gameLogic.location.LocationRegistry;
import com.example.game.gameLogic.records.*;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AirTrafficEvent implements GameEvent {

    private final AirTrafficProvider airTrafficProvider;
    private final LocationRegistry locationRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public EventType getType() {
        return EventType.AIR_TRAFFIC_BUZZ;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        return true;
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        GameLocation location = locationRegistry.getByIndex(gameState.getLocationIndex());
        AirTrafficData trafficData = airTrafficProvider.getAirTraffic(location);
        Traffic traffic = TrafficClassifier.classify(trafficData.nearbyAircraftCount());

        return new PendingEvent(
                EventType.AIR_TRAFFIC_BUZZ,
                "Air Traffic Buzz",
                buildDescription(location, trafficData, traffic),
                List.of(
                        new EventOption(
                                EventOptionType.PUSH_FORWARD,
                                "Push forward",
                                "Lean into the city's energy and keep moving."
                        ),
                        new EventOption(
                                EventOptionType.REST,
                                "Lay low",
                                "Avoid the chaos and regroup."
                        )
                )
        );
    }

    private String buildDescription(GameLocation location, AirTrafficData trafficData, Traffic traffic) {
        String sourceNote = trafficData.fallback() ? " Fallback traffic data was used." : "";

        String humanText = switch (traffic) {
            case QUIET -> "The skies over " + location.name()
                    + " are unusually calm. Only " + trafficData.nearbyAircraftCount()
                    + " nearby aircraft are showing up." + sourceNote;
            case MODERATE -> "Air traffic around " + location.name()
                    + " is steady, with about " + trafficData.nearbyAircraftCount()
                    + " nearby aircraft." + sourceNote;
            case BUSY -> location.name()
                    + " feels alive. Roughly " + trafficData.nearbyAircraftCount()
                    + " aircraft are moving through nearby airspace." + sourceNote;
            case JAMMED -> "The airspace near " + location.name()
                    + " is packed. Around " + trafficData.nearbyAircraftCount()
                    + " aircraft are nearby, and the whole region feels intense." + sourceNote;
        };

        String metadata = " [traffic_count=" + trafficData.nearbyAircraftCount()
                + "][fallback=" + trafficData.fallback() + "]";

        return humanText + metadata;
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        AirTrafficData trafficData = readTrafficData(gameState);
        Traffic traffic = TrafficClassifier.classify(trafficData.nearbyAircraftCount());

        return switch (optionType) {
            case PUSH_FORWARD -> resolvePushForward(gameState, traffic, trafficData.nearbyAircraftCount());
            case REST -> resolveRest(gameState, traffic, trafficData.nearbyAircraftCount());
            default -> throw new IllegalArgumentException("Invalid option for AIR_TRAFFIC_BUZZ");
        };
    }

    private EventResult resolvePushForward(GameState gameState, Traffic traffic, int aircraftCount) {
        return switch (traffic) {
            case QUIET -> {
                int bugsFixed = 2;
                gameState.setBugs(Math.max(0, gameState.getBugs() - bugsFixed));

                yield new EventResult(
                        EventType.AIR_TRAFFIC_BUZZ,
                        "PUSH_FORWARD",
                        "The calm skies helped the team focus.",
                        List.of("-" + bugsFixed + " bugs")
                );
            }
            case MODERATE -> {
                int motivationGain = 4;
                gameState.setMotivation(gameState.getMotivation() + motivationGain);

                yield new EventResult(
                        EventType.AIR_TRAFFIC_BUZZ,
                        "PUSH_FORWARD",
                        "The steady city rhythm kept everyone productive.",
                        List.of("+" + motivationGain + " motivation")
                );
            }
            case BUSY -> {
                int cashGain = 40;
                int coffeeCost = 1;

                gameState.setCash(gameState.getCash() + cashGain);
                gameState.setCoffee(Math.max(0, gameState.getCoffee() - coffeeCost));

                yield new EventResult(
                        EventType.AIR_TRAFFIC_BUZZ,
                        "PUSH_FORWARD",
                        "The busy energy opened a few small opportunities, but it drained the team.",
                        List.of(
                                "+" + cashGain + " cash",
                                "-" + coffeeCost + " coffee"
                        )
                );
            }
            case JAMMED -> {
                int motivationLoss = 4;
                int bugsAdded = 2;

                gameState.setMotivation(gameState.getMotivation() - motivationLoss);
                gameState.setBugs(gameState.getBugs() + bugsAdded);

                yield new EventResult(
                        EventType.AIR_TRAFFIC_BUZZ,
                        "PUSH_FORWARD",
                        "The airspace frenzy made everything feel chaotic.",
                        List.of(
                                "-" + motivationLoss + " motivation",
                                "+" + bugsAdded + " bugs"
                        )
                );
            }
        };
    }

    private EventResult resolveRest(GameState gameState, Traffic traffic, int aircraftCount) {
        return switch (traffic) {
            case QUIET -> {
                int motivationGain = 2;
                gameState.setMotivation(gameState.getMotivation() + motivationGain);

                yield new EventResult(
                        EventType.AIR_TRAFFIC_BUZZ,
                        "REST",
                        "The quiet atmosphere helped the team reset.",
                        List.of("+" + motivationGain + " motivation")
                );
            }
            case MODERATE -> {
                int coffeeGain = 1;
                gameState.setCoffee(gameState.getCoffee() + coffeeGain);

                yield new EventResult(
                        EventType.AIR_TRAFFIC_BUZZ,
                        "REST",
                        "A measured pause helped the team recharge.",
                        List.of("+" + coffeeGain + " coffee")
                );
            }
            case BUSY -> {
                int cashCost = 15;
                int motivationGain = 3;

                gameState.setCash(gameState.getCash() - cashCost);
                gameState.setMotivation(gameState.getMotivation() + motivationGain);

                yield new EventResult(
                        EventType.AIR_TRAFFIC_BUZZ,
                        "REST",
                        "You stepped away from the noise and bought the team some breathing room.",
                        List.of(
                                "-" + cashCost + " cash",
                                "+" + motivationGain + " motivation"
                        )
                );
            }
            case JAMMED -> {
                int coffeeGain = 2;
                int bugsFixed = 1;

                gameState.setCoffee(gameState.getCoffee() + coffeeGain);
                gameState.setBugs(Math.max(0, gameState.getBugs() - bugsFixed));

                yield new EventResult(
                        EventType.AIR_TRAFFIC_BUZZ,
                        "REST",
                        "Laying low during the chaos prevented worse mistakes.",
                        List.of(
                                "+" + coffeeGain + " coffee",
                                "-" + bugsFixed + " bugs"
                        )
                );
            }
        };
    }

    private AirTrafficData readTrafficData(GameState gameState) {
        try {
            PendingEvent pendingEvent = objectMapper.readValue(
                    gameState.getPendingEventJson(),
                    PendingEvent.class
            );

            String description = pendingEvent.description();

            return new AirTrafficData(
                    extractAircraftCount(description),
                    extractFallback(description)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read air traffic event data", e);
        }
    }

    private int extractAircraftCount(String description) {
        Matcher matcher = Pattern.compile("\\[traffic_count=(\\d+)]").matcher(description);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        throw new IllegalStateException(
                "Could not extract aircraft count from description: " + description
        );
    }

    private boolean extractFallback(String description) {
        Matcher matcher = Pattern.compile("\\[fallback=(true|false)]").matcher(description);

        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }

        throw new IllegalStateException(
                "Could not extract fallback flag from description: " + description
        );
    }
}
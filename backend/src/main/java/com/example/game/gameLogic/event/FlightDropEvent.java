package com.example.game.gameLogic.event;

import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.event.api.FlightActivityClient;
import com.example.game.gameLogic.location.GameLocation;
import com.example.game.gameLogic.location.LocationRegistry;
import com.example.game.gameLogic.records.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FlightDropEvent implements GameEvent {

    private final FlightActivityClient flightActivityClient;
    private final LocationRegistry locationRegistry;
    private final RandomProvider randomProvider;

    @Override
    public EventType getType() {
        return EventType.FLIGHT_DROP;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        GameLocation location = locationRegistry.getByIndex(gameState.getLocationIndex());
        return flightActivityClient.hasNearbyFlightActivity(location.latitude(), location.longitude());
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        return new PendingEvent(
                EventType.FLIGHT_DROP,
                "Flight Drop Opportunity",
                "Heavy travel activity nearby created a last-minute opportunity. Do you chase it or stay focused?",
                List.of(
                        new EventOption(EventOptionType.ACCEPT, "Chase opportunity", "Spend energy for a possible cash boost."),
                        new EventOption(EventOptionType.DECLINE, "Ignore it", "Stay on plan.")
                )
        );
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        return switch (optionType) {
            case ACCEPT -> {
                int motivationCost = 4;
                gameState.setMotivation(gameState.getMotivation() - motivationCost);

                int cashGain = randomProvider.nextIntInclusive(50, 120);
                gameState.setCash(gameState.getCash() + cashGain);

                yield new EventResult(
                        EventType.FLIGHT_DROP,
                        "ACCEPT",
                        "You chased the opportunity and turned travel buzz into a small win.",
                        List.of(
                                "+" + cashGain + " cash",
                                "-" + motivationCost + " motivation"
                        )
                );
            }
            case DECLINE -> new EventResult(
                    EventType.FLIGHT_DROP,
                    "DECLINE",
                    "You ignored the distraction and kept moving.",
                    List.of()
            );
            default -> throw new IllegalArgumentException("Invalid option for FLIGHT_DROP");
        };
    }
}

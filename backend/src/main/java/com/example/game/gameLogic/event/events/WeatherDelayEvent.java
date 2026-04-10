package com.example.game.gameLogic.event.events;



import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.event.records.EventOptionType;
import com.example.game.gameLogic.event.api.WeatherClient;
import com.example.game.gameLogic.event.records.EventOption;
import com.example.game.gameLogic.event.records.EventResult;
import com.example.game.gameLogic.event.records.GameEvent;
import com.example.game.gameLogic.event.records.PendingEvent;
import com.example.game.gameLogic.location.GameLocation;
import com.example.game.gameLogic.location.LocationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WeatherDelayEvent implements GameEvent {

    private final WeatherClient weatherClient;
    private final LocationRegistry locationRegistry;
    private final RandomProvider randomProvider;

    @Override
    public EventType getType() {
        return EventType.WEATHER_DELAY;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        GameLocation location = locationRegistry.getByIndex(gameState.getLocationIndex());
        return weatherClient.hasTravelDelayRisk(location.latitude(), location.longitude());
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        return new PendingEvent(
                EventType.WEATHER_DELAY,
                "Weather Delay",
                "Bad weather is slowing things down. Push through or wait it out?",
                List.of(
                        new EventOption(EventOptionType.PUSH_THROUGH, "Push through", "Lose extra morale and coffee."),
                        new EventOption(EventOptionType.PLAY_IT_SAFE, "Wait it out", "Lose some time but avoid heavier strain.")
                )
        );
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        return switch (optionType) {
            case PUSH_THROUGH -> {
                int coffeeLoss = randomProvider.nextIntInclusive(2, 4);
                int motivationLoss = randomProvider.nextIntInclusive(4, 7);

                gameState.setCoffee(gameState.getCoffee() - coffeeLoss);
                gameState.setMotivation(gameState.getMotivation() - motivationLoss);

                yield new EventResult(
                        EventType.WEATHER_DELAY,
                        "PUSH_THROUGH",
                        "You forced your way through the weather, but the team felt it.",
                        List.of(
                                "-" + coffeeLoss + " coffee",
                                "-" + motivationLoss + " motivation"
                        )
                );
            }
            case PLAY_IT_SAFE -> {
                int motivationLoss = randomProvider.nextIntInclusive(1, 3);
                gameState.setMotivation(gameState.getMotivation() - motivationLoss);

                yield new EventResult(
                        EventType.WEATHER_DELAY,
                        "PLAY_IT_SAFE",
                        "You waited for better conditions and kept the damage manageable.",
                        List.of("-" + motivationLoss + " motivation")
                );
            }
            default -> throw new IllegalArgumentException("Invalid option for WEATHER_DELAY");
        };
    }
}

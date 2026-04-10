package com.example.game.gameLogic.event.events;


import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.event.records.EventOptionType;
import com.example.game.gameLogic.event.records.EventOption;
import com.example.game.gameLogic.event.records.EventResult;
import com.example.game.gameLogic.event.records.GameEvent;
import com.example.game.gameLogic.event.records.PendingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ViralTweetEvent implements GameEvent {

    private final RandomProvider randomProvider;

    @Override
    public EventType getType() {
        return EventType.VIRAL_TWEET;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        return randomProvider.chance(0.12);
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        return new PendingEvent(
                EventType.VIRAL_TWEET,
                "Viral Tweet",
                "A tweet about your startup is blowing up. Monetize the moment or just ride the wave?",
                List.of(
                        new EventOption(EventOptionType.ACCEPT, "Monetize it", "Push for cash, at some morale cost."),
                        new EventOption(EventOptionType.PASS, "Ride the wave", "Take a smaller pure morale boost.")
                )
        );
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        return switch (optionType) {
            case ACCEPT -> {
                int cashGain = randomProvider.nextIntInclusive(100, 180);
                int motivationGain = randomProvider.nextIntInclusive(3, 6);

                gameState.setCash(gameState.getCash() + cashGain);
                gameState.setMotivation(gameState.getMotivation() + motivationGain);

                yield new EventResult(
                        EventType.VIRAL_TWEET,
                        "ACCEPT",
                        "You capitalized on the attention and turned hype into momentum.",
                        List.of(
                                "+" + cashGain + " cash",
                                "+" + motivationGain + " motivation"
                        )
                );
            }
            case PASS -> {
                int motivationGain = randomProvider.nextIntInclusive(6, 12);
                gameState.setMotivation(gameState.getMotivation() + motivationGain);

                yield new EventResult(
                        EventType.VIRAL_TWEET,
                        "PASS",
                        "You let the moment boost the team without chasing money.",
                        List.of("+" + motivationGain + " motivation")
                );
            }
            default -> throw new IllegalArgumentException("Invalid option for VIRAL_TWEET");
        };
    }
}

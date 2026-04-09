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
public class HackathonNearbyEvent implements GameEvent {

    private final RandomProvider randomProvider;

    @Override
    public EventType getType() {
        return EventType.HACKATHON_NEARBY;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        return randomProvider.chance(0.10);
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        return new PendingEvent(
                EventType.HACKATHON_NEARBY,
                "Hackathon Nearby",
                "A local hackathon is happening. Join it for upside, or keep moving?",
                List.of(
                        new EventOption(EventOptionType.ACCEPT, "Join hackathon", "Possible cash and bug fixes, but tiring."),
                        new EventOption(EventOptionType.DECLINE, "Skip it", "Avoid the detour.")
                )
        );
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        return switch (optionType) {
            case ACCEPT -> {
                int cashGain = randomProvider.nextIntInclusive(40, 120);
                int bugsReduced = randomProvider.nextIntInclusive(1, 3);
                int motivationLoss = randomProvider.nextIntInclusive(2, 5);

                gameState.setCash(gameState.getCash() + cashGain);
                gameState.setBugs(Math.max(0, gameState.getBugs() - bugsReduced));
                gameState.setMotivation(gameState.getMotivation() - motivationLoss);

                yield new EventResult(
                        EventType.HACKATHON_NEARBY,
                        "ACCEPT",
                        "The team squeezed value out of the hackathon, but it took a toll.",
                        List.of(
                                "+" + cashGain + " cash",
                                "-" + bugsReduced + " bugs",
                                "-" + motivationLoss + " motivation"
                        )
                );
            }
            case DECLINE -> new EventResult(
                    EventType.HACKATHON_NEARBY,
                    "DECLINE",
                    "You skipped the side quest and stayed on route.",
                    List.of()
            );
            default -> throw new IllegalArgumentException("Invalid option for HACKATHON_NEARBY");
        };
    }
}

package com.example.game.gameLogic.event;


import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.records.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenSourceDramaEvent implements GameEvent {

    private final RandomProvider randomProvider;

    @Override
    public EventType getType() {
        return EventType.OPEN_SOURCE_DRAMA;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        return randomProvider.chance(0.09);
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        return new PendingEvent(
                EventType.OPEN_SOURCE_DRAMA,
                "Open Source Drama",
                "A dependency you rely on just imploded online. Patch immediately or work around it later?",
                List.of(
                        new EventOption(EventOptionType.REPAIR_NOW, "Patch now", "Spend energy now to reduce future pain."),
                        new EventOption(EventOptionType.DELAY_REPAIR, "Deal with it later", "Save energy now, risk more bugs.")
                )
        );
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        return switch (optionType) {
            case REPAIR_NOW -> {
                int coffeeLoss = randomProvider.nextIntInclusive(3, 5);
                int bugsAdded = randomProvider.nextIntInclusive(1, 2);

                gameState.setCoffee(gameState.getCoffee() - coffeeLoss);
                gameState.setBugs(gameState.getBugs() + bugsAdded);

                yield new EventResult(
                        EventType.OPEN_SOURCE_DRAMA,
                        "REPAIR_NOW",
                        "You contained the damage, but it cost the team energy.",
                        List.of(
                                "-" + coffeeLoss + " coffee",
                                "+" + bugsAdded + " bugs"
                        )
                );
            }
            case DELAY_REPAIR -> {
                int bugsAdded = randomProvider.nextIntInclusive(3, 5);
                int motivationLoss = randomProvider.nextIntInclusive(2, 4);

                gameState.setBugs(gameState.getBugs() + bugsAdded);
                gameState.setMotivation(gameState.getMotivation() - motivationLoss);

                yield new EventResult(
                        EventType.OPEN_SOURCE_DRAMA,
                        "DELAY_REPAIR",
                        "You kicked the can down the road and the problem grew.",
                        List.of(
                                "+" + bugsAdded + " bugs",
                                "-" + motivationLoss + " motivation"
                        )
                );
            }
            default -> throw new IllegalArgumentException("Invalid option for OPEN_SOURCE_DRAMA");
        };
    }
}

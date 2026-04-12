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
public class HardwareFailureEvent implements GameEvent {

    private final RandomProvider randomProvider;

    @Override
    public EventType getType() {
        return EventType.HARDWARE_FAILURE;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        return randomProvider.chance(0.08);
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        return new PendingEvent(
                EventType.HARDWARE_FAILURE,
                "Hardware Failure",
                "Something critical broke. Repair it now or limp forward and hope it holds?",
                List.of(
                        new EventOption(EventOptionType.REPAIR_NOW, "Repair now", "Pay more cash, reduce bug fallout."),
                        new EventOption(EventOptionType.DELAY_REPAIR, "Limp forward", "Save some cash, risk more bugs.")
                )
        );
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        return switch (optionType) {
            case REPAIR_NOW -> {
                int cashLoss = randomProvider.nextIntInclusive(80, 130);
                int bugsAdded = randomProvider.nextIntInclusive(0, 1);

                gameState.setCash(gameState.getCash() - cashLoss);
                gameState.setBugs(gameState.getBugs() + bugsAdded);

                yield new EventResult(
                        EventType.HARDWARE_FAILURE,
                        "REPAIR_NOW",
                        "You paid to fix it immediately and limited the damage.",
                        List.of(
                                "-" + cashLoss + " cash",
                                "+" + bugsAdded + " bugs"
                        )
                );
            }
            case DELAY_REPAIR -> {
                int cashLoss = randomProvider.nextIntInclusive(30, 60);
                int bugsAdded = randomProvider.nextIntInclusive(2, 4);

                gameState.setCash(gameState.getCash() - cashLoss);
                gameState.setBugs(gameState.getBugs() + bugsAdded);

                yield new EventResult(
                        EventType.HARDWARE_FAILURE,
                        "DELAY_REPAIR",
                        "You delayed the repair and the system degraded further.",
                        List.of(
                                "-" + cashLoss + " cash",
                                "+" + bugsAdded + " bugs"
                        )
                );
            }
            default -> throw new IllegalArgumentException("Invalid option for HARDWARE_FAILURE");
        };
    }
}

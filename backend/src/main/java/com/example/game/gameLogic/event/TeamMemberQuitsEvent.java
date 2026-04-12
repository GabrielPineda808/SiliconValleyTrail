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
public class TeamMemberQuitsEvent implements GameEvent {

    private final RandomProvider randomProvider;

    @Override
    public EventType getType() {
        return EventType.TEAM_MEMBER_QUITS;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        return randomProvider.chance(0.05);
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        return new PendingEvent(
                EventType.TEAM_MEMBER_QUITS,
                "Team Member Quits",
                "A key teammate is leaving. Try to retain them or accept the loss?",
                List.of(
                        new EventOption(EventOptionType.INVEST, "Try to retain them", "Spend cash for a chance to soften the hit."),
                        new EventOption(EventOptionType.ACCEPT, "Accept it", "Take the morale and bug hit.")
                )
        );
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        return switch (optionType) {
            case INVEST -> {
                int cashLoss = randomProvider.nextIntInclusive(60, 120);
                gameState.setCash(gameState.getCash() - cashLoss);

                boolean success = randomProvider.chance(0.50);
                if (success) {
                    int motivationLoss = randomProvider.nextIntInclusive(3, 6);
                    gameState.setMotivation(gameState.getMotivation() - motivationLoss);

                    yield new EventResult(
                            EventType.TEAM_MEMBER_QUITS,
                            "INVEST",
                            "You contained the fallout, but it still hurt morale.",
                            List.of(
                                    "-" + cashLoss + " cash",
                                    "-" + motivationLoss + " motivation"
                            )
                    );
                }

                int motivationLoss = randomProvider.nextIntInclusive(8, 14);
                int bugsAdded = randomProvider.nextIntInclusive(2, 4);

                gameState.setMotivation(gameState.getMotivation() - motivationLoss);
                gameState.setBugs(gameState.getBugs() + bugsAdded);

                yield new EventResult(
                        EventType.TEAM_MEMBER_QUITS,
                        "INVEST",
                        "The retention effort failed and the loss still hit hard.",
                        List.of(
                                "-" + cashLoss + " cash",
                                "-" + motivationLoss + " motivation",
                                "+" + bugsAdded + " bugs"
                        )
                );
            }
            case ACCEPT -> {
                int motivationLoss = randomProvider.nextIntInclusive(8, 16);
                int bugsAdded = randomProvider.nextIntInclusive(2, 4);

                gameState.setMotivation(gameState.getMotivation() - motivationLoss);
                gameState.setBugs(gameState.getBugs() + bugsAdded);

                yield new EventResult(
                        EventType.TEAM_MEMBER_QUITS,
                        "ACCEPT",
                        "You absorbed the loss and kept moving, but the damage was real.",
                        List.of(
                                "-" + motivationLoss + " motivation",
                                "+" + bugsAdded + " bugs"
                        )
                );
            }
            default -> throw new IllegalArgumentException("Invalid option for TEAM_MEMBER_QUITS");
        };
    }
}

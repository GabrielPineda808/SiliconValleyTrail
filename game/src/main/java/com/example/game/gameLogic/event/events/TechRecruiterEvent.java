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
public class TechRecruiterEvent implements GameEvent {

    private final RandomProvider randomProvider;

    @Override
    public EventType getType() {
        return EventType.TECH_RECRUITER;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        return randomProvider.chance(0.09);
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        return new PendingEvent(
                EventType.TECH_RECRUITER,
                "Tech Recruiter",
                "A recruiter reaches out with tempting options. Entertain it or stay focused?",
                List.of(
                        new EventOption(EventOptionType.ACCEPT, "Hear them out", "Might bring quick money, but drains focus."),
                        new EventOption(EventOptionType.DECLINE, "Ignore recruiter", "Stay locked in.")
                )
        );
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        return switch (optionType) {
            case ACCEPT -> {
                int cashGain = randomProvider.nextIntInclusive(70, 140);
                int motivationLoss = randomProvider.nextIntInclusive(4, 8);

                gameState.setCash(gameState.getCash() + cashGain);
                gameState.setMotivation(gameState.getMotivation() - motivationLoss);

                yield new EventResult(
                        EventType.TECH_RECRUITER,
                        "ACCEPT",
                        "The conversation paid off a little, but it pulled the team off mission.",
                        List.of(
                                "+" + cashGain + " cash",
                                "-" + motivationLoss + " motivation"
                        )
                );
            }
            case DECLINE -> new EventResult(
                    EventType.TECH_RECRUITER,
                    "DECLINE",
                    "You ignored the recruiter and kept your attention where it belongs.",
                    List.of()
            );
            default -> throw new IllegalArgumentException("Invalid option for TECH_RECRUITER");
        };
    }
}

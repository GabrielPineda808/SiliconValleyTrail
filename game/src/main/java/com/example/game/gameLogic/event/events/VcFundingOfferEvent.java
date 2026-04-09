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
public class VcFundingOfferEvent implements GameEvent {

    private final RandomProvider randomProvider;

    @Override
    public EventType getType() {
        return EventType.VC_FUNDING_OFFER;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        return randomProvider.chance(0.07);
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        return new PendingEvent(
                EventType.VC_FUNDING_OFFER,
                "VC Funding Offer",
                "An investor is willing to hear your pitch. Take the meeting or stay focused?",
                List.of(
                        new EventOption(EventOptionType.ACCEPT, "Take meeting", "Risk motivation for a shot at serious cash."),
                        new EventOption(EventOptionType.DECLINE, "Decline", "Avoid the distraction.")
                )
        );
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        return switch (optionType) {
            case ACCEPT -> {
                int motivationCost = 8;
                gameState.setMotivation(gameState.getMotivation() - motivationCost);

                boolean success = randomProvider.chance(0.45);
                if (success) {
                    int cashGain = randomProvider.nextIntInclusive(180, 350);
                    gameState.setCash(gameState.getCash() + cashGain);

                    yield new EventResult(
                            EventType.VC_FUNDING_OFFER,
                            "ACCEPT",
                            "The pitch worked. Funding came through.",
                            List.of(
                                    "+" + cashGain + " cash",
                                    "-" + motivationCost + " motivation"
                            )
                    );
                }

                yield new EventResult(
                        EventType.VC_FUNDING_OFFER,
                        "ACCEPT",
                        "The investor passed.",
                        List.of("-" + motivationCost + " motivation")
                );
            }
            case DECLINE -> new EventResult(
                    EventType.VC_FUNDING_OFFER,
                    "DECLINE",
                    "You stayed focused and skipped the meeting.",
                    List.of()
            );
            default -> throw new IllegalArgumentException("Invalid option for VC_FUNDING_OFFER");
        };
    }
}

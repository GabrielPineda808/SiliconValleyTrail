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
public class CoffeeShopDealEvent implements GameEvent {

    private final RandomProvider randomProvider;

    @Override
    public EventType getType() {
        return EventType.COFFEE_SHOP_DEAL;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        return randomProvider.chance(0.11);
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        return new PendingEvent(
                EventType.COFFEE_SHOP_DEAL,
                "Coffee Shop Deal",
                "A local cafe offers a startup-friendly deal. Take it or pass?",
                List.of(
                        new EventOption(EventOptionType.TAKE_DEAL, "Take the deal", "Spend a little cash for a strong coffee refill."),
                        new EventOption(EventOptionType.PASS, "Pass", "Save cash.")
                )
        );
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        return switch (optionType) {
            case TAKE_DEAL -> {
                int cashCost = randomProvider.nextIntInclusive(20, 40);
                int coffeeGain = randomProvider.nextIntInclusive(8, 15);

                gameState.setCash(gameState.getCash() - cashCost);
                gameState.setCoffee(gameState.getCoffee() + coffeeGain);

                yield new EventResult(
                        EventType.COFFEE_SHOP_DEAL,
                        "TAKE_DEAL",
                        "You took the deal and restocked the team’s fuel.",
                        List.of(
                                "-" + cashCost + " cash",
                                "+" + coffeeGain + " coffee"
                        )
                );
            }
            case PASS -> new EventResult(
                    EventType.COFFEE_SHOP_DEAL,
                    "PASS",
                    "You passed on the deal and conserved cash.",
                    List.of()
            );
            default -> throw new IllegalArgumentException("Invalid option for COFFEE_SHOP_DEAL");
        };
    }
}

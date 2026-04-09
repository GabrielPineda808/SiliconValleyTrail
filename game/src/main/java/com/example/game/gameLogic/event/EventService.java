package com.example.game.gameLogic.event;

import com.example.game.entity.GameState;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.action.ActionResult;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EventService {
    private final ObjectMapper objectMapper;
    private final RandomProvider randomProvider;

    public PendingEvent triggerArrivalEvents(GameState gameState) {
        double roll = randomProvider.nextDouble(0,1);

        PendingEvent event = null;

        if (roll < 0.15) {
            event = new PendingEvent(
                    "VC_FUNDING_OFFER",
                    "VC Funding Offer",
                    "A VC unexpectedly offers to invest in your startup.",
                    List.of(
                            new EventChoice("ACCEPT", "Accept the offer"),
                            new EventChoice("DECLINE", "Decline the offer")
                    )
            );
        } else if (roll < 0.30) {
            event = new PendingEvent(
                    "HACKATHON_NEARBY",
                    "Hackathon Nearby",
                    "There is a local hackathon happening right now.",
                    List.of(
                            new EventChoice("JOIN", "Join the hackathon"),
                            new EventChoice("SKIP", "Skip it")
                    )
            );
        }

        if (event != null) {
            persistPendingEvent(gameState, event);
        }

        return event;
    }

    public EventResolutionResult resolveEvent(GameState gameState, String choiceCode) {
        if (!gameState.isEventPending() || gameState.getPendingEventType() == null || gameState.getPendingEventJson() == null) {
            throw new InvalidActionException("No pending event to resolve");
        }

        PendingEvent event = readPendingEvent(gameState);

        ActionResult result = switch (event.type()) {
            case "VC_FUNDING_OFFER" -> resolveVcFundingOffer(gameState, choiceCode);
            case "HACKATHON_NEARBY" -> resolveHackathonNearby(gameState, choiceCode);
            default -> throw new InvalidActionException("Unknown event type: " + event.type());
        };

        clearPendingEvent(gameState);
        return new EventResolutionResult(gameState, result);
    }

    private ActionResult resolveVcFundingOffer(GameState gameState, String choiceCode) {
        return switch (choiceCode) {
            case "ACCEPT" -> {
                gameState.setCash(gameState.getCash() + 300);
                gameState.setBugs(gameState.getBugs() + 2);
                yield new ActionResult(
                        "You accepted the VC funding offer.",
                        List.of("+300 cash", "+2 bugs"),
                        true
                );
            }
            case "DECLINE" -> {
                gameState.setMotivation(gameState.getMotivation() + 5);
                yield new ActionResult(
                        "You declined the VC funding offer and stayed independent.",
                        List.of("+5 motivation"),
                        true
                );
            }
            default -> throw new InvalidActionException("Invalid event choice");
        };
    }

    private ActionResult resolveHackathonNearby(GameState gameState, String choiceCode) {
        return switch (choiceCode) {
            case "JOIN" -> {
                boolean win = randomProvider.nextDouble(0,1) < 0.5;

                if (win) {
                    gameState.setCash(gameState.getCash() + 200);
                    gameState.setBugs(Math.max(0, gameState.getBugs() - 3));
                    yield new ActionResult(
                            "You crushed the hackathon.",
                            List.of("+200 cash", "-3 bugs"),
                            true
                    );
                } else {
                    gameState.setBugs(gameState.getBugs() + 3);
                    gameState.setMotivation(Math.max(0, gameState.getMotivation() - 5));
                    yield new ActionResult(
                            "The hackathon went badly.",
                            List.of("+3 bugs", "-5 motivation"),
                            false
                    );
                }
            }
            case "SKIP" -> new ActionResult(
                    "You skipped the hackathon.",
                    List.of(),
                    true
            );
            default -> throw new InvalidActionException("Invalid event choice");
        };
    }

    private void persistPendingEvent(GameState gameState, PendingEvent event) {
        gameState.setPendingEventType(event.type());

        try {
            gameState.setPendingEventJson(objectMapper.writeValueAsString(event));
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize pending event", e);
        }

        gameState.setEventPending(true);
    }

    private PendingEvent readPendingEvent(GameState gameState) {
        try {
            return objectMapper.readValue(gameState.getPendingEventJson(), PendingEvent.class);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to deserialize pending event", e);
        }
    }

    private void clearPendingEvent(GameState gameState) {
        gameState.setPendingEventType(null);
        gameState.setPendingEventJson(null);
        gameState.setEventPending(false);
    }
}

package com.example.game.gameLogic.action;

import com.example.game.entity.GameState;
import com.example.game.exceptions.InvalidActionException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActionResolverTest {

    @Test
    void resolveReturnsMatchingActionImplementation() {
        PlayerAction travel = new StubAction(ActionType.TRAVEL);
        PlayerAction rest = new StubAction(ActionType.REST);
        ActionResolver resolver = new ActionResolver(List.of(travel, rest));

        assertThat(resolver.resolve(ActionType.TRAVEL)).isSameAs(travel);
        assertThat(resolver.resolve(ActionType.REST)).isSameAs(rest);
    }

    @Test
    void resolveThrowsWhenActionTypeIsUnsupported() {
        ActionResolver resolver = new ActionResolver(List.of(new StubAction(ActionType.REST)));

        assertThatThrownBy(() -> resolver.resolve(ActionType.TRAVEL))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("Unsupported action: TRAVEL");
    }

    private record StubAction(ActionType type) implements PlayerAction {
        @Override
        public ActionType getType() {
            return type;
        }

        @Override
        public boolean canExecute(GameState gameState) {
            return true;
        }

        @Override
        public ActionResult execute(GameState gameState) {
            return new ActionResult("ok", List.of(), false);
        }
    }
}

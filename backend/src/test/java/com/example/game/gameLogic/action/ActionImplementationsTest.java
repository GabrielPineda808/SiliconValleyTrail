package com.example.game.gameLogic.action;

import com.example.game.entity.GameState;
import com.example.game.enums.GameStatus;
import com.example.game.exceptions.InvalidActionException;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.action.buySupplies.BuySuppliesAction;
import com.example.game.gameLogic.action.fixBug.FixBugAction;
import com.example.game.gameLogic.action.freelance.FreelanceAction;
import com.example.game.gameLogic.action.pitchVC.PitchVcAction;
import com.example.game.gameLogic.action.rest.RestAction;
import com.example.game.gameLogic.action.travel.GasCalc;
import com.example.game.gameLogic.action.travel.TravelAction;
import com.example.game.gameLogic.location.GameLocation;
import com.example.game.gameLogic.location.LocationRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionImplementationsTest {

    @Mock
    private RandomProvider randomProvider;

    @Mock
    private LocationRegistry locationRegistry;

    @Mock
    private GasCalc gasCalc;

    @Test
    void travelActionChecksBoundariesAndUpdatesState() {
        TravelAction action = new TravelAction(locationRegistry, gasCalc);
        GameState gameState = baseState();
        when(locationRegistry.lastIndex()).thenReturn(2);
        when(locationRegistry.getByIndex(0)).thenReturn(new GameLocation("San Jose", 1.0, 1.0));
        when(locationRegistry.getByIndex(1)).thenReturn(new GameLocation("Santa Clara", 2.0, 2.0));
        when(gasCalc.calculateGasCost(1.0, 1.0, 2.0, 2.0)).thenReturn(15);

        assertThat(action.canExecute(gameState)).isTrue();

        ActionResult result = action.execute(gameState);

        assertThat(gameState.getGas()).isEqualTo(85);
        assertThat(gameState.getCoffee()).isEqualTo(48);
        assertThat(gameState.getMotivation()).isEqualTo(97);
        assertThat(gameState.getLocationIndex()).isEqualTo(1);
        assertThat(gameState.getLocationName()).isEqualTo("Santa Clara");
        assertThat(result.travelOccurred()).isTrue();
        assertThat(result.effects()).containsExactly("-15 gas", "-2 coffee", "-3 motivation");
    }

    @Test
    void travelActionRejectsLastStopAndInsufficientResources() {
        TravelAction action = new TravelAction(locationRegistry, gasCalc);
        GameState gameState = baseState();
        when(locationRegistry.lastIndex()).thenReturn(0);

        assertThat(action.canExecute(gameState)).isFalse();
        assertThatThrownBy(() -> action.execute(gameState))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("You can't execute this action");
    }

    @Test
    void restActionRequiresCashAndUsesDeterministicRecovery() {
        RestAction action = new RestAction(randomProvider);
        GameState gameState = baseState();
        when(randomProvider.nextIntInclusive(5, 10)).thenReturn(7);
        when(randomProvider.nextIntInclusive(8, 15)).thenReturn(12);

        ActionResult result = action.execute(gameState);

        assertThat(gameState.getCash()).isEqualTo(440);
        assertThat(gameState.getCoffee()).isEqualTo(57);
        assertThat(gameState.getMotivation()).isEqualTo(112);
        assertThat(result.effects()).containsExactly("-60 cash", "+7 coffee", "+12 motivation");
    }

    @Test
    void restActionThrowsWhenCashIsTooLow() {
        RestAction action = new RestAction(randomProvider);
        GameState gameState = baseState();
        gameState.setCash(59);

        assertThat(action.canExecute(gameState)).isFalse();
        assertThatThrownBy(() -> action.execute(gameState))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("Cannot rest. Not enough cash.");
    }

    @Test
    void fixBugActionConsumesCoffeeAndCapsBugsFixedAtCurrentTotal() {
        FixBugAction action = new FixBugAction(randomProvider);
        GameState gameState = baseState();
        gameState.setBugs(2);
        gameState.setCoffee(3);
        when(randomProvider.nextIntInclusive(1, 4)).thenReturn(4);

        ActionResult result = action.execute(gameState);

        assertThat(gameState.getBugs()).isZero();
        assertThat(gameState.getCoffee()).isZero();
        assertThat(result.effects()).containsExactly("-3 coffee", "-2 bugs");
    }

    @Test
    void fixBugActionThrowsWhenThereAreNoBugs() {
        FixBugAction action = new FixBugAction(randomProvider);
        GameState gameState = baseState();
        gameState.setBugs(0);

        assertThat(action.canExecute(gameState)).isFalse();
        assertThatThrownBy(() -> action.execute(gameState))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("Cannot fix bugs. Not enough coffee or no bugs to fix.");
    }

    @Test
    void freelanceActionConsumesResourcesAndAwardsCash() {
        FreelanceAction action = new FreelanceAction(randomProvider);
        GameState gameState = baseState();
        when(randomProvider.nextIntInclusive(80, 180)).thenReturn(120);

        ActionResult result = action.execute(gameState);

        assertThat(gameState.getCash()).isEqualTo(620);
        assertThat(gameState.getCoffee()).isEqualTo(47);
        assertThat(gameState.getMotivation()).isEqualTo(95);
        assertThat(result.effects()).containsExactly("+120 cash", "-3 coffee", "-5 motivation");
    }

    @Test
    void freelanceActionThrowsWhenResourcesAreInsufficient() {
        FreelanceAction action = new FreelanceAction(randomProvider);
        GameState gameState = baseState();
        gameState.setCoffee(2);

        assertThat(action.canExecute(gameState)).isFalse();
        assertThatThrownBy(() -> action.execute(gameState))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("Cannot freelance. Not enough coffee or motivation.");
    }

    @Test
    void buySuppliesActionConsumesCashAndAddsGasAndCoffee() {
        BuySuppliesAction action = new BuySuppliesAction(randomProvider);
        GameState gameState = baseState();
        when(randomProvider.nextIntInclusive(10, 18)).thenReturn(16);
        when(randomProvider.nextIntInclusive(6, 12)).thenReturn(9);

        ActionResult result = action.execute(gameState);

        assertThat(gameState.getCash()).isEqualTo(425);
        assertThat(gameState.getGas()).isEqualTo(116);
        assertThat(gameState.getCoffee()).isEqualTo(59);
        assertThat(result.effects()).containsExactly("-75 cash", "+16 gas", "+9 coffee");
    }

    @Test
    void buySuppliesActionThrowsWhenCashIsInsufficient() {
        BuySuppliesAction action = new BuySuppliesAction(randomProvider);
        GameState gameState = baseState();
        gameState.setCash(74);

        assertThatThrownBy(() -> action.execute(gameState))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("Cannot buy supplies. Not enough cash.");
    }

    @Test
    void pitchVcActionHandlesSuccessAndFailureBranches() {
        PitchVcAction action = new PitchVcAction(randomProvider);

        GameState successState = baseState();
        when(randomProvider.chance(0.40)).thenReturn(true);
        when(randomProvider.nextIntInclusive(250, 450)).thenReturn(300);
        ActionResult successResult = action.execute(successState);

        assertThat(successState.getCash()).isEqualTo(800);
        assertThat(successState.getMotivation()).isEqualTo(88);
        assertThat(successResult.message()).isEqualTo("The VC pitch landed. Funding secured.");
        assertThat(successResult.effects()).containsExactly("+300 cash", "-12 motivation");

        GameState failureState = baseState();
        when(randomProvider.chance(0.40)).thenReturn(false);
        ActionResult failureResult = action.execute(failureState);

        assertThat(failureState.getCash()).isEqualTo(500);
        assertThat(failureState.getMotivation()).isEqualTo(88);
        assertThat(failureResult.message()).isEqualTo("The VC pitch failed.");
        assertThat(failureResult.effects()).containsExactly("-12 motivation");
    }

    @Test
    void pitchVcActionThrowsWhenMotivationIsTooLow() {
        PitchVcAction action = new PitchVcAction(randomProvider);
        GameState gameState = baseState();
        gameState.setMotivation(11);

        assertThat(action.canExecute(gameState)).isFalse();
        assertThatThrownBy(() -> action.execute(gameState))
                .isInstanceOf(InvalidActionException.class)
                .hasMessage("Cannot pitch VC. Not enough motivation.");
    }

    private static GameState baseState() {
        return GameState.builder()
                .gas(100)
                .cash(500)
                .bugs(4)
                .coffee(50)
                .motivation(100)
                .locationIndex(0)
                .locationName("San Jose")
                .day(1)
                .status(GameStatus.IN_PROGRESS)
                .coffeeZeroStreak(0)
                .eventPending(false)
                .build();
    }
}

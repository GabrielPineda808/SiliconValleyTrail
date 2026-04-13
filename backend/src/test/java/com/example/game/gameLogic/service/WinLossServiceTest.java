package com.example.game.gameLogic.service;

import com.example.game.entity.GameState;
import com.example.game.enums.GameStatus;
import com.example.game.gameLogic.location.LocationRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WinLossServiceTest {

    @Mock
    private LocationRegistry locationRegistry;

    @InjectMocks
    private WinLossService winLossService;

    @Test
    void evaluateMarksGameWonAtFinalLocation() {
        GameState gameState = baseState();
        gameState.setLocationIndex(10);
        when(locationRegistry.lastIndex()).thenReturn(10);

        winLossService.evaluate(gameState);

        assertThat(gameState.getStatus()).isEqualTo(GameStatus.WON);
    }

    @Test
    void evaluateMarksGameLostWhenGasRunsOut() {
        GameState gameState = baseState();
        gameState.setGas(0);
        when(locationRegistry.lastIndex()).thenReturn(10);

        winLossService.evaluate(gameState);

        assertThat(gameState.getStatus()).isEqualTo(GameStatus.LOST);
        assertThat(winLossService.getLossReason(gameState)).isEqualTo("You ran out of gas.");
    }

    @Test
    void evaluateMarksGameLostWhenCashRunsOut() {
        GameState gameState = baseState();
        gameState.setCash(0);
        when(locationRegistry.lastIndex()).thenReturn(10);

        winLossService.evaluate(gameState);

        assertThat(gameState.getStatus()).isEqualTo(GameStatus.LOST);
        assertThat(winLossService.getLossReason(gameState)).isEqualTo("You ran out of cash.");
    }

    @Test
    void evaluateMarksGameLostWhenTooManyBugsAccumulate() {
        GameState gameState = baseState();
        gameState.setBugs(15);
        when(locationRegistry.lastIndex()).thenReturn(10);

        winLossService.evaluate(gameState);

        assertThat(gameState.getStatus()).isEqualTo(GameStatus.LOST);
        assertThat(winLossService.getLossReason(gameState)).isEqualTo("Too many bugs accumulated.");
    }

    @Test
    void evaluateMarksGameLostWhenMotivationHitsZero() {
        GameState gameState = baseState();
        gameState.setMotivation(0);
        when(locationRegistry.lastIndex()).thenReturn(10);

        winLossService.evaluate(gameState);

        assertThat(gameState.getStatus()).isEqualTo(GameStatus.LOST);
        assertThat(winLossService.getLossReason(gameState)).isEqualTo("The team lost all motivation.");
    }

    @Test
    void evaluateMarksGameLostWhenCoffeeZeroStreakIsTooHigh() {
        GameState gameState = baseState();
        gameState.setCoffeeZeroStreak(2);
        when(locationRegistry.lastIndex()).thenReturn(10);

        winLossService.evaluate(gameState);

        assertThat(gameState.getStatus()).isEqualTo(GameStatus.LOST);
        assertThat(winLossService.getLossReason(gameState)).isEqualTo("The team went without coffee for too long.");
    }

    @Test
    void evaluateLeavesGameInProgressWhenNoWinOrLossConditionIsMet() {
        GameState gameState = baseState();
        when(locationRegistry.lastIndex()).thenReturn(10);

        winLossService.evaluate(gameState);

        assertThat(gameState.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(winLossService.getLossReason(gameState)).isNull();
    }

    private static GameState baseState() {
        return GameState.builder()
                .gas(100)
                .cash(500)
                .bugs(2)
                .coffee(20)
                .motivation(50)
                .locationIndex(1)
                .locationName("Santa Clara")
                .day(3)
                .status(GameStatus.IN_PROGRESS)
                .coffeeZeroStreak(0)
                .eventPending(false)
                .build();
    }
}

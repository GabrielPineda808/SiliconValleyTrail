package com.example.game.gameLogic.event;

import com.example.game.entity.GameState;
import com.example.game.enums.GameStatus;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.records.EventOptionType;
import com.example.game.gameLogic.records.EventResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoryEventsTest {

    @Mock
    private RandomProvider randomProvider;

    @Test
    void techRecruiterEventResolvesAcceptAndDecline() {
        TechRecruiterEvent event = new TechRecruiterEvent(randomProvider);
        GameState acceptState = baseState();
        when(randomProvider.nextIntInclusive(70, 140)).thenReturn(100);
        when(randomProvider.nextIntInclusive(4, 8)).thenReturn(5);

        EventResult acceptResult = event.resolve(acceptState, EventOptionType.ACCEPT);

        assertThat(acceptState.getCash()).isEqualTo(600);
        assertThat(acceptState.getMotivation()).isEqualTo(75);
        assertThat(acceptResult.effects()).containsExactly("+100 cash", "-5 motivation");

        GameState declineState = baseState();
        EventResult declineResult = event.resolve(declineState, EventOptionType.DECLINE);

        assertThat(declineState.getCash()).isEqualTo(500);
        assertThat(declineResult.outcome()).isEqualTo("You ignored the recruiter and kept your attention where it belongs.");
    }

    @Test
    void viralTweetEventResolvesAcceptAndPass() {
        ViralTweetEvent event = new ViralTweetEvent(randomProvider);
        GameState acceptState = baseState();
        when(randomProvider.nextIntInclusive(100, 180)).thenReturn(150);
        when(randomProvider.nextIntInclusive(3, 6)).thenReturn(4);

        EventResult acceptResult = event.resolve(acceptState, EventOptionType.ACCEPT);

        assertThat(acceptState.getCash()).isEqualTo(650);
        assertThat(acceptState.getMotivation()).isEqualTo(84);
        assertThat(acceptResult.effects()).containsExactly("+150 cash", "+4 motivation");

        GameState passState = baseState();
        when(randomProvider.nextIntInclusive(6, 12)).thenReturn(9);

        EventResult passResult = event.resolve(passState, EventOptionType.PASS);

        assertThat(passState.getMotivation()).isEqualTo(89);
        assertThat(passResult.effects()).containsExactly("+9 motivation");
    }

    @Test
    void hardwareFailureEventResolvesRepairNowAndDelayRepair() {
        HardwareFailureEvent event = new HardwareFailureEvent(randomProvider);
        GameState repairState = baseState();
        when(randomProvider.nextIntInclusive(80, 130)).thenReturn(90);
        when(randomProvider.nextIntInclusive(0, 1)).thenReturn(1);

        EventResult repairResult = event.resolve(repairState, EventOptionType.REPAIR_NOW);

        assertThat(repairState.getCash()).isEqualTo(410);
        assertThat(repairState.getBugs()).isEqualTo(3);
        assertThat(repairResult.effects()).containsExactly("-90 cash", "+1 bugs");

        GameState delayState = baseState();
        when(randomProvider.nextIntInclusive(30, 60)).thenReturn(45);
        when(randomProvider.nextIntInclusive(2, 4)).thenReturn(3);

        EventResult delayResult = event.resolve(delayState, EventOptionType.DELAY_REPAIR);

        assertThat(delayState.getCash()).isEqualTo(455);
        assertThat(delayState.getBugs()).isEqualTo(5);
        assertThat(delayResult.effects()).containsExactly("-45 cash", "+3 bugs");
    }

    @Test
    void hackathonNearbyEventResolvesAcceptAndDecline() {
        HackathonNearbyEvent event = new HackathonNearbyEvent(randomProvider);
        GameState acceptState = baseState();
        when(randomProvider.nextIntInclusive(40, 120)).thenReturn(90);
        when(randomProvider.nextIntInclusive(1, 3)).thenReturn(2);
        when(randomProvider.nextIntInclusive(2, 5)).thenReturn(4);

        EventResult acceptResult = event.resolve(acceptState, EventOptionType.ACCEPT);

        assertThat(acceptState.getCash()).isEqualTo(590);
        assertThat(acceptState.getBugs()).isZero();
        assertThat(acceptState.getMotivation()).isEqualTo(76);
        assertThat(acceptResult.effects()).containsExactly("+90 cash", "-2 bugs", "-4 motivation");

        GameState declineState = baseState();
        EventResult declineResult = event.resolve(declineState, EventOptionType.DECLINE);

        assertThat(declineResult.effects()).isEmpty();
        assertThat(declineState.getCash()).isEqualTo(500);
    }

    @Test
    void vcFundingOfferEventResolvesAcceptSuccessFailureAndDecline() {
        VcFundingOfferEvent event = new VcFundingOfferEvent(randomProvider);
        GameState successState = baseState();
        when(randomProvider.chance(0.45)).thenReturn(true);
        when(randomProvider.nextIntInclusive(180, 350)).thenReturn(220);

        EventResult successResult = event.resolve(successState, EventOptionType.ACCEPT);

        assertThat(successState.getCash()).isEqualTo(720);
        assertThat(successState.getMotivation()).isEqualTo(72);
        assertThat(successResult.effects()).containsExactly("+220 cash", "-8 motivation");

        GameState failureState = baseState();
        when(randomProvider.chance(0.45)).thenReturn(false);

        EventResult failureResult = event.resolve(failureState, EventOptionType.ACCEPT);

        assertThat(failureState.getCash()).isEqualTo(500);
        assertThat(failureState.getMotivation()).isEqualTo(72);
        assertThat(failureResult.effects()).containsExactly("-8 motivation");

        GameState declineState = baseState();
        EventResult declineResult = event.resolve(declineState, EventOptionType.DECLINE);

        assertThat(declineResult.effects()).isEmpty();
        assertThat(declineState.getMotivation()).isEqualTo(80);
    }

    @Test
    void coffeeShopDealEventResolvesTakeDealAndPass() {
        CoffeeShopDealEvent event = new CoffeeShopDealEvent(randomProvider);
        GameState dealState = baseState();
        when(randomProvider.nextIntInclusive(20, 40)).thenReturn(25);
        when(randomProvider.nextIntInclusive(8, 15)).thenReturn(11);

        EventResult dealResult = event.resolve(dealState, EventOptionType.TAKE_DEAL);

        assertThat(dealState.getCash()).isEqualTo(475);
        assertThat(dealState.getCoffee()).isEqualTo(21);
        assertThat(dealResult.effects()).containsExactly("-25 cash", "+11 coffee");

        GameState passState = baseState();
        EventResult passResult = event.resolve(passState, EventOptionType.PASS);

        assertThat(passResult.effects()).isEmpty();
        assertThat(passState.getCash()).isEqualTo(500);
    }

    @Test
    void openSourceDramaEventResolvesRepairNowAndDelayRepair() {
        OpenSourceDramaEvent event = new OpenSourceDramaEvent(randomProvider);
        GameState repairState = baseState();
        when(randomProvider.nextIntInclusive(3, 5)).thenReturn(4);
        when(randomProvider.nextIntInclusive(1, 2)).thenReturn(2);

        EventResult repairResult = event.resolve(repairState, EventOptionType.REPAIR_NOW);

        assertThat(repairState.getCoffee()).isEqualTo(6);
        assertThat(repairState.getBugs()).isEqualTo(4);
        assertThat(repairResult.effects()).containsExactly("-4 coffee", "+2 bugs");

        GameState delayState = baseState();
        when(randomProvider.nextIntInclusive(3, 5)).thenReturn(5);
        when(randomProvider.nextIntInclusive(2, 4)).thenReturn(3);

        EventResult delayResult = event.resolve(delayState, EventOptionType.DELAY_REPAIR);

        assertThat(delayState.getBugs()).isEqualTo(7);
        assertThat(delayState.getMotivation()).isEqualTo(77);
        assertThat(delayResult.effects()).containsExactly("+5 bugs", "-3 motivation");
    }

    @Test
    void teamMemberQuitsEventResolvesInvestSuccessFailureAndAccept() {
        TeamMemberQuitsEvent event = new TeamMemberQuitsEvent(randomProvider);
        GameState investSuccessState = baseState();
        when(randomProvider.nextIntInclusive(60, 120)).thenReturn(70);
        when(randomProvider.chance(0.50)).thenReturn(true);
        when(randomProvider.nextIntInclusive(3, 6)).thenReturn(4);

        EventResult investSuccess = event.resolve(investSuccessState, EventOptionType.INVEST);

        assertThat(investSuccessState.getCash()).isEqualTo(430);
        assertThat(investSuccessState.getMotivation()).isEqualTo(76);
        assertThat(investSuccess.effects()).containsExactly("-70 cash", "-4 motivation");

        GameState investFailureState = baseState();
        when(randomProvider.nextIntInclusive(60, 120)).thenReturn(80);
        when(randomProvider.chance(0.50)).thenReturn(false);
        when(randomProvider.nextIntInclusive(8, 14)).thenReturn(10);
        when(randomProvider.nextIntInclusive(2, 4)).thenReturn(3);

        EventResult investFailure = event.resolve(investFailureState, EventOptionType.INVEST);

        assertThat(investFailureState.getCash()).isEqualTo(420);
        assertThat(investFailureState.getMotivation()).isEqualTo(70);
        assertThat(investFailureState.getBugs()).isEqualTo(5);
        assertThat(investFailure.effects()).containsExactly("-80 cash", "-10 motivation", "+3 bugs");

        GameState acceptState = baseState();
        when(randomProvider.nextIntInclusive(8, 16)).thenReturn(11);
        when(randomProvider.nextIntInclusive(2, 4)).thenReturn(2);

        EventResult acceptResult = event.resolve(acceptState, EventOptionType.ACCEPT);

        assertThat(acceptState.getMotivation()).isEqualTo(69);
        assertThat(acceptState.getBugs()).isEqualTo(4);
        assertThat(acceptResult.effects()).containsExactly("-11 motivation", "+2 bugs");
    }

    private static GameState baseState() {
        return GameState.builder()
                .gas(100)
                .cash(500)
                .bugs(2)
                .coffee(10)
                .motivation(80)
                .locationIndex(1)
                .locationName("Santa Clara")
                .day(3)
                .status(GameStatus.IN_PROGRESS)
                .coffeeZeroStreak(0)
                .eventPending(false)
                .build();
    }
}

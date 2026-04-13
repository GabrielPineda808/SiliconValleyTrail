package com.example.game.gameLogic.event.flightAPI;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficClassifierTest {

    @Test
    void classifyUsesExpectedTrafficBandsAcrossBoundaries() {
        assertThat(TrafficClassifier.classify(6)).isEqualTo(Traffic.QUIET);
        assertThat(TrafficClassifier.classify(7)).isEqualTo(Traffic.MODERATE);
        assertThat(TrafficClassifier.classify(14)).isEqualTo(Traffic.MODERATE);
        assertThat(TrafficClassifier.classify(15)).isEqualTo(Traffic.BUSY);
        assertThat(TrafficClassifier.classify(22)).isEqualTo(Traffic.BUSY);
        assertThat(TrafficClassifier.classify(23)).isEqualTo(Traffic.JAMMED);
    }
}

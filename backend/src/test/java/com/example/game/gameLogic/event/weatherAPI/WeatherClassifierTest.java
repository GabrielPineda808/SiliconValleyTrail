package com.example.game.gameLogic.event.weatherAPI;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherClassifierTest {

    private final WeatherClassifier classifier = new WeatherClassifier();

    @Test
    void classifyMapsRepresentativeWeatherCodesToExpectedBands() {
        assertThat(classifier.classify(0)).isEqualTo(Weather.CLEAR);
        assertThat(classifier.classify(3)).isEqualTo(Weather.CLOUDY);
        assertThat(classifier.classify(45)).isEqualTo(Weather.FOGGY);
        assertThat(classifier.classify(53)).isEqualTo(Weather.DRIZZLE);
        assertThat(classifier.classify(61)).isEqualTo(Weather.RAIN);
        assertThat(classifier.classify(95)).isEqualTo(Weather.THUNDER);
        assertThat(classifier.classify(999)).isEqualTo(Weather.CLOUDY);
    }
}

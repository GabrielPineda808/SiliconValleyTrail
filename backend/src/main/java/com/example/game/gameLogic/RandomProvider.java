package com.example.game.gameLogic;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@AllArgsConstructor
public class RandomProvider {

    public int nextIntInclusive(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public boolean chance(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    public double nextDouble(double minInclusive, double maxExclusive) {
        return ThreadLocalRandom.current().nextDouble(minInclusive, maxExclusive);
    }
}

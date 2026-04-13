package com.example.game.gameLogic.event;

import com.example.game.entity.GameState;
import com.example.game.enums.EventType;
import com.example.game.gameLogic.RandomProvider;
import com.example.game.gameLogic.event.weatherAPI.Weather;
import com.example.game.gameLogic.event.weatherAPI.WeatherClassifier;
import com.example.game.gameLogic.event.weatherAPI.WeatherData;
import com.example.game.gameLogic.event.weatherAPI.WeatherProvider;
import com.example.game.gameLogic.location.GameLocation;
import com.example.game.gameLogic.location.LocationRegistry;
import com.example.game.gameLogic.records.*;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WeatherDelayEvent implements GameEvent {

    private static final Pattern TEMPERATURE_PATTERN =
            Pattern.compile("\\[temperature_f=(\\d+(?:\\.\\d+)?)\\]");

    private static final Pattern WEATHER_CODE_PATTERN =
            Pattern.compile("\\[weather_code=(-?\\d+)\\]");

    private static final Pattern FALLBACK_PATTERN =
            Pattern.compile("\\[fallback=(true|false)\\]");

    private final WeatherProvider weatherProvider;
    private final LocationRegistry locationRegistry;
    private final WeatherClassifier weatherClassifier = new WeatherClassifier();
    private final RandomProvider randomProvider;
    private final ObjectMapper objectMapper;

    @Override
    public EventType getType() {
        return EventType.WEATHER_SHIFT;
    }

    @Override
    public boolean canTrigger(GameState gameState) {
        return true;
    }

    @Override
    public PendingEvent createPendingEvent(GameState gameState) {
        GameLocation location = locationRegistry.getByIndex(gameState.getLocationIndex());
        WeatherData weatherData = weatherProvider.getWeather(location);
        Weather weather = weatherClassifier.classify(weatherData.weatherCode());
        String title = "Weather Shift";
        String description = buildDescription(location, weatherData, weather);

        List<EventOption> choices = switch (weather) {
            case CLEAR -> List.of(
                    new EventOption(
                            EventOptionType.PUSH_FORWARD,
                            "Push forward",
                            "The clear weather boosts morale. Gain motivation, but spend a little coffee."
                    ),
                    new EventOption(
                            EventOptionType.REST,
                            "Take a breather",
                            "Use the calm weather to recover."
                    )
            );
            case CLOUDY -> List.of(
                    new EventOption(
                            EventOptionType.PUSH_FORWARD,
                            "Keep working",
                            "Nothing special. Steady progress with mild resource drain."
                    ),
                    new EventOption(
                            EventOptionType.REST,
                            "Regroup",
                            "Play it safe and recover a little."
                    )
            );
            case FOGGY -> List.of(
                    new EventOption(
                            EventOptionType.PUSH_FORWARD,
                            "Navigate through fog",
                            "Travel is stressful. Lose motivation and maybe extra gas."
                    ),
                    new EventOption(
                            EventOptionType.REST,
                            "Wait for visibility",
                            "Lose a day of momentum but avoid risky travel."
                    )
            );
            case DRIZZLE -> List.of(
                    new EventOption(
                            EventOptionType.PUSH_FORWARD,
                            "Work through the drizzle",
                            "Minor annoyance. Lose a little motivation."
                    ),
                    new EventOption(
                            EventOptionType.REST,
                            "Duck into a cafe",
                            "Spend cash, gain coffee."
                    )
            );
            case RAIN -> List.of(
                    new EventOption(
                            EventOptionType.PUSH_FORWARD,
                            "Push through the rain",
                            "Bad conditions increase fatigue and slow the team."
                    ),
                    new EventOption(
                            EventOptionType.REST,
                            "Wait it out",
                            "Conserve energy and avoid extra bugs from rushed work."
                    )
            );
            case THUNDER -> List.of(
                    new EventOption(
                            EventOptionType.PUSH_FORWARD,
                            "Risk it anyway",
                            "Very dangerous. Big potential penalties."
                    ),
                    new EventOption(
                            EventOptionType.REST,
                            "Shelter in place",
                            "Avoid major damage but sacrifice momentum."
                    )
            );
        };

        return new PendingEvent(
                EventType.WEATHER_SHIFT,
                title,
                description,
                choices
        );
    }

    private String buildDescription(GameLocation location, WeatherData weatherData, Weather weather) {
        String source = weatherData.fallback() ? " Fallback weather data was used." : "";

        String humanText = switch (weather) {
            case CLEAR -> "The skies over " + location.name() + " are clear and it's " + weatherData.temperature()
                    + "F. Conditions are excellent for momentum." + source;
            case CLOUDY -> "Cloud cover hangs over " + location.name() + " at " + weatherData.temperature()
                    + "F. Not terrible, not inspiring." + source;
            case FOGGY -> "Dense fog sits over " + location.name() + " and the temperature is "
                    + weatherData.temperature() + "F. Visibility is poor." + source;
            case DRIZZLE -> "A light drizzle falls in " + location.name() + " at " + weatherData.temperature()
                    + "F. The team can work through it, but morale dips." + source;
            case RAIN -> "Rain is hitting " + location.name() + " and it's " + weatherData.temperature()
                    + "F. Conditions are rough." + source;
            case THUNDER -> "Thunderstorms are rolling through " + location.name() + " at "
                    + weatherData.temperature() + "F. This could wreck the day." + source;
        };

        String metadata = " [temperature_f=" + weatherData.temperature()
                + "][weather_code=" + weatherData.weatherCode()
                + "][fallback=" + weatherData.fallback() + "]";

        return humanText + metadata;
    }

    @Override
    public EventResult resolve(GameState gameState, EventOptionType optionType) {
        WeatherData weatherData = readWeatherEventData(gameState);
        Weather weather = weatherClassifier.classify(weatherData.weatherCode());

        return switch (optionType) {
            case PUSH_FORWARD -> resolvePushForward(gameState, weather);
            case REST -> resolveRest(gameState, weather);
            default -> throw new IllegalArgumentException("Invalid option for WEATHER_SHIFT");
        };
    }

    private EventResult resolvePushForward(GameState gameState, Weather weather) {
        return switch (weather) {
            case CLEAR -> {
                int motivationGain = randomProvider.nextIntInclusive(4, 7);
                int coffeeCost = 1;

                gameState.setMotivation(gameState.getMotivation() + motivationGain);
                gameState.setCoffee(Math.max(0, gameState.getCoffee() - coffeeCost));

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "PUSH_FORWARD",
                        "Clear weather helped the team move fast.",
                        List.of(
                                "+" + motivationGain + " motivation",
                                "-" + coffeeCost + " coffee"
                        )
                );
            }
            case CLOUDY -> {
                int bugsReduced = randomProvider.nextIntInclusive(1, 2);

                gameState.setBugs(Math.max(0, gameState.getBugs() - bugsReduced));

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "PUSH_FORWARD",
                        "The team stayed steady despite the gray skies.",
                        List.of("-" + bugsReduced + " bugs")
                );
            }
            case FOGGY -> {
                int motivationLoss = randomProvider.nextIntInclusive(2, 4);
                int gasLoss = randomProvider.nextIntInclusive(4, 8);

                gameState.setMotivation(gameState.getMotivation() - motivationLoss);
                gameState.setGas(gameState.getGas() - gasLoss);

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "PUSH_FORWARD",
                        "Poor visibility made progress stressful and inefficient.",
                        List.of(
                                "-" + motivationLoss + " motivation",
                                "-" + gasLoss + " gas"
                        )
                );
            }
            case DRIZZLE -> {
                int motivationLoss = randomProvider.nextIntInclusive(1, 3);

                gameState.setMotivation(gameState.getMotivation() - motivationLoss);

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "PUSH_FORWARD",
                        "The drizzle slowed the team's momentum.",
                        List.of("-" + motivationLoss + " motivation")
                );
            }
            case RAIN -> {
                int coffeeCost = 2;
                int motivationLoss = randomProvider.nextIntInclusive(2, 5);

                gameState.setCoffee(Math.max(0, gameState.getCoffee() - coffeeCost));
                gameState.setMotivation(gameState.getMotivation() - motivationLoss);

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "PUSH_FORWARD",
                        "Rain made the day draining and expensive in focus.",
                        List.of(
                                "-" + coffeeCost + " coffee",
                                "-" + motivationLoss + " motivation"
                        )
                );
            }
            case THUNDER -> {
                int bugIncrease = randomProvider.nextIntInclusive(2, 5);
                int motivationLoss = randomProvider.nextIntInclusive(4, 7);

                gameState.setBugs(gameState.getBugs() + bugIncrease);
                gameState.setMotivation(gameState.getMotivation() - motivationLoss);

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "PUSH_FORWARD",
                        "The storm wrecked focus and caused messy work.",
                        List.of(
                                "+" + bugIncrease + " bugs",
                                "-" + motivationLoss + " motivation"
                        )
                );
            }
        };
    }

    private EventResult resolveRest(GameState gameState, Weather weather) {
        return switch (weather) {
            case CLEAR -> {
                int motivationGain = randomProvider.nextIntInclusive(2, 4);

                gameState.setMotivation(gameState.getMotivation() + motivationGain);

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "REST",
                        "The team recovered well in the pleasant weather.",
                        List.of("+" + motivationGain + " motivation")
                );
            }
            case CLOUDY -> {
                int coffeeGain = 1;

                gameState.setCoffee(gameState.getCoffee() + coffeeGain);

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "REST",
                        "A quiet break helped everyone reset.",
                        List.of("+" + coffeeGain + " coffee")
                );
            }
            case FOGGY -> {
                int motivationGain = randomProvider.nextIntInclusive(1, 3);

                gameState.setMotivation(gameState.getMotivation() + motivationGain);

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "REST",
                        "Waiting out the fog prevented worse losses.",
                        List.of("+" + motivationGain + " motivation")
                );
            }
            case DRIZZLE -> {
                int cashCost = randomProvider.nextIntInclusive(10, 20);
                int coffeeGain = randomProvider.nextIntInclusive(1, 2);

                gameState.setCash(gameState.getCash() - cashCost);
                gameState.setCoffee(gameState.getCoffee() + coffeeGain);

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "REST",
                        "The team ducked into a cafe and regrouped.",
                        List.of(
                                "-" + cashCost + " cash",
                                "+" + coffeeGain + " coffee"
                        )
                );
            }
            case RAIN -> {
                int bugsReduced = randomProvider.nextIntInclusive(1, 2);

                gameState.setBugs(Math.max(0, gameState.getBugs() - bugsReduced));

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "REST",
                        "Waiting out the rain gave the team time to clean things up.",
                        List.of("-" + bugsReduced + " bugs")
                );
            }

            case THUNDER -> {
                int coffeeGain = 1;
                int motivationGain = randomProvider.nextIntInclusive(2, 5);

                gameState.setCoffee(gameState.getCoffee() + coffeeGain);
                gameState.setMotivation(gameState.getMotivation() + motivationGain);

                yield new EventResult(
                        EventType.WEATHER_SHIFT,
                        "REST",
                        "Sheltering from the storm helped the team recover.",
                        List.of(
                                "+" + coffeeGain + " coffee",
                                "+" + motivationGain + " motivation"
                        )
                );
            }
        };
    }

    private WeatherData readWeatherEventData(GameState gameState) {
        try {
            PendingEvent pendingEvent = objectMapper.readValue(
                    gameState.getPendingEventJson(),
                    PendingEvent.class
            );

            String description = pendingEvent.description();

            return new WeatherData(
                    extractTemperature(description),
                    extractWeatherCode(description),
                    extractFallback(description)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read weather event data", e);
        }
    }

    private double extractTemperature(String description) {
        Matcher matcher = TEMPERATURE_PATTERN.matcher(description);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }

        throw new IllegalStateException("Could not extract temperature from description: " + description);
    }

    private int extractWeatherCode(String description) {
        Matcher matcher = WEATHER_CODE_PATTERN.matcher(description);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        throw new IllegalStateException("Could not extract weather code from description: " + description);
    }

    private boolean extractFallback(String description) {
        Matcher matcher = FALLBACK_PATTERN.matcher(description);

        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }

        throw new IllegalStateException("Could not extract fallback flag from description: " + description);
    }
}
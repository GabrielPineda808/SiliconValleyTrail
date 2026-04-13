package com.example.game.repository;

import com.example.game.entity.GameState;
import com.example.game.entity.User;
import com.example.game.enums.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GameStateRepoDataJpaTest {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GameStateRepo gameStateRepo;

    @BeforeEach
    void cleanDatabase() {
        gameStateRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void findStateByUserAndStatusReturnsOnlyMatchingActiveState() {
        User user = userRepo.save(user("gabe"));
        gameStateRepo.save(gameState(user, GameStatus.LOST, "San Jose"));
        GameState active = gameStateRepo.save(gameState(user, GameStatus.IN_PROGRESS, "Santa Clara"));

        GameState loaded = gameStateRepo.findStateByUserAndStatus(user, GameStatus.IN_PROGRESS).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(active.getId());
        assertThat(loaded.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(loaded.getLocationName()).isEqualTo("Santa Clara");
    }

    @Test
    void existsByUserAndStatusAndDeleteByUserAndStatusTargetOnlyRequestedStatus() {
        User user = userRepo.save(user("gabe"));
        gameStateRepo.save(gameState(user, GameStatus.IN_PROGRESS, "San Jose"));
        gameStateRepo.save(gameState(user, GameStatus.WON, "San Francisco"));

        assertThat(gameStateRepo.existsByUserAndStatus(user, GameStatus.IN_PROGRESS)).isTrue();

        gameStateRepo.deleteByUserAndStatus(user, GameStatus.IN_PROGRESS);

        assertThat(gameStateRepo.existsByUserAndStatus(user, GameStatus.IN_PROGRESS)).isFalse();
        assertThat(gameStateRepo.existsByUserAndStatus(user, GameStatus.WON)).isTrue();
    }

    private static User user(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvabcdefghijklmnopqrstuvabcdefgh");
        return user;
    }

    private static GameState gameState(User user, GameStatus status, String locationName) {
        return GameState.builder()
                .user(user)
                .gas(100)
                .cash(500)
                .bugs(1)
                .coffee(10)
                .motivation(80)
                .locationIndex(0)
                .locationName(locationName)
                .day(1)
                .status(status)
                .coffeeZeroStreak(0)
                .eventPending(false)
                .build();
    }
}

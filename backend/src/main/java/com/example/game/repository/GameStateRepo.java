package com.example.game.repository;

import com.example.game.entity.GameState;
import com.example.game.entity.User;
import com.example.game.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameStateRepo extends JpaRepository<GameState, Integer> {
    Optional<GameState> findByUser(User user);
    boolean existsByUserId(Long userId);
    void deleteByUser(User user);
    boolean existsByUserAndStatus(User user, GameStatus gameStatus);
    Optional<GameState> findStateByUserAndStatus(User user, GameStatus gameStatus);
    void deleteByUserAndStatus(User user, GameStatus gameStatus);
}

package com.example.game.repository;

import com.example.game.entity.GameState;
import com.example.game.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameStateRepo extends JpaRepository<GameState, Integer> {
    Optional<GameState> findByUser(User user);
    boolean existsByUserId(Long userId);
    void deleteByUser(User user);
}

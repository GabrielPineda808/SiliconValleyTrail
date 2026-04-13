package com.example.game.entity;

import com.example.game.audit.AuditableEntity;
import com.example.game.enums.GameStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "game_state")
public class GameState extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int gas;

    @Column(nullable = false)
    private int cash;

    @Column(nullable = false)
    private int bugs;

    @Column(nullable = false)
    private int coffee;

    @Column(nullable = false)
    private int motivation;

    @Column(name = "location_index", nullable = false)
    private int locationIndex;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(name = "game_day", nullable = false)
    private int day;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status;

    @Column(name = "coffee_zero_streak", nullable = false)
    private int coffeeZeroStreak;

    @Column(name = "state_json", columnDefinition = "TEXT")
    private String stateJson;

    @Column(columnDefinition = "TEXT")
    private String pendingEventJson;

    @Builder.Default
    @Column(nullable = false)
    private boolean eventPending = false;
}


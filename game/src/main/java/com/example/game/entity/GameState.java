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
@Table(
        name = "game_state",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_slot", columnNames = {"user_id", "slot_number"})
        }
)
public class GameState extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer gas;

    @Column(nullable = false)
    private Integer cash;

    @Column(nullable = false)
    private Integer bugs;

    @Column(nullable = false)
    private Integer coffee;

    @Column(nullable = false)
    private Integer motivation;

    @Column(name = "location_index", nullable = false)
    private Integer locationIndex;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(nullable = false)
    private Integer day;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status;

    @Column(name = "coffee_zero_streak", nullable = false)
    private Integer coffeeZeroStreak;

    @Column(name = "state_json", columnDefinition = "TEXT")
    private String stateJson;

    @Column
    private String pendingEventType;
    @Column
    private String pendingEventJson;

    @Column(nullable = false)
    private boolean eventPending = false;
}


package com.example.game.dto.request;

import com.example.game.enums.ActionType;
import jakarta.validation.constraints.NotNull;

public record PerformActionRequest (
        @NotNull
        ActionType action
){
}

package com.example.game.gameLogic.action;

import java.util.List;

public record ActionResult (
        String message,
        List<String> effects,
        boolean travelOccurred
){
    public static ActionResult from(String message, List<String> effects, boolean travelOccurred) {
        return new ActionResult(message, effects, travelOccurred);
    }
}

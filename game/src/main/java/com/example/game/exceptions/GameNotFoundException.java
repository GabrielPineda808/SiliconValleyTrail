package com.example.game.exceptions;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String message) {
        super(message);
    }
    public GameNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

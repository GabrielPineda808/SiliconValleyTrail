package com.example.game.exceptions;

public class GameExistsException extends RuntimeException {
    public GameExistsException(String message) {
        super(message);
    }
    public GameExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}

package com.example.game.exceptions;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Centralized mapping from application exceptions to API error responses.
 */
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex, HttpServletRequest request
    ) {
        log.warn("User not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "USER_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(GameExistsException.class)
    public ResponseEntity<ErrorResponse> handleGameExistsException(
            GameExistsException ex, HttpServletRequest request
    ) {
        log.warn("Game already exists: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "GAME_ALREADY_EXISTS",
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGameNotFoundException(
            GameNotFoundException ex, HttpServletRequest request
    ) {
        log.warn("Game not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "GAME_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidActionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidActionException(
            InvalidActionException ex, HttpServletRequest request
    ) {
        log.warn("Cannot perform action: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_ACTION",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NotFoundException ex, HttpServletRequest request
    ) {
        log.warn("Not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        log.warn("Validation failed: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorResponse.FieldError(
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_FAILED",
                "Request validation failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request
    ) {
        log.warn("Request body could not be read: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_REQUEST",
                "Request body is malformed or missing required values",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request
    ) {
        log.error("Unhandled exception", ex);
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
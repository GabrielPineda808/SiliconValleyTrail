package com.example.game.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error payload returned by the API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response returned by the API.")
public record ErrorResponse(
        @Schema(description = "Machine-readable error code.", example = "VALIDATION_FAILED")
        String error,

        @Schema(description = "Human-readable error summary.", example = "Request validation failed")
        String message,

        @Schema(description = "HTTP status code.", example = "400")
        int status,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "Timestamp when the error was generated.", example = "2026-04-09 17:56:04")
        LocalDateTime timestamp,

        @Schema(description = "Request path that produced the error.", example = "/auth/signup")
        String path,

        @Schema(description = "Field-level validation errors, if applicable.")
        List<FieldError> fieldErrors
) {
    public ErrorResponse(String error, String message, int status) {
        this(error, message, status, LocalDateTime.now(), null, null);
    }

    public ErrorResponse(String error, String message, int status, String path) {
        this(error, message, status, LocalDateTime.now(), path, null);
    }

    public ErrorResponse(String error, String message, int status, String path, List<FieldError> fieldErrors) {
        this(error, message, status, LocalDateTime.now(), path, fieldErrors);
    }

    /**
     * Field-level validation detail for a request payload.
     */
    @Schema(description = "Validation error associated with a single request field.")
    public record FieldError(
            @Schema(description = "Name of the field that failed validation.", example = "username")
            String field,

            @Schema(description = "Rejected value provided by the client.", example = "")
            Object rejectedValue,

            @Schema(description = "Validation message for the field.", example = "Username is required")
            String message
    ) {}
}
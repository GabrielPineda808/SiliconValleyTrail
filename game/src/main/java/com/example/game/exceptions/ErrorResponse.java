package com.example.game.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error payload returned by the API.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response returned by the API.")
public class ErrorResponse {
    @Schema(description = "Machine-readable error code.", example = "VALIDATION_FAILED")
    private String error;

    @Schema(description = "Human-readable error summary.", example = "Request validation failed")
    private String message;

    @Schema(description = "HTTP status code.", example = "400")
    private int status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyy-MM-dd HH:mm:ss")
    @Schema(description = "Timestamp when the error was generated.", example = "2026-04-09 17:56:04")
    private LocalDateTime timestamp;

    @Schema(description = "Request path that produced the error.", example = "/auth/signup")
    private String path;

    @Schema(description = "Field-level validation errors, if applicable.")
    private List<FieldError> fieldErrors;

    public ErrorResponse() {this.timestamp = LocalDateTime.now();}


    public ErrorResponse(String error, String message, int status) {
        this();
        this.error = error;
        this.message = message;
        this.status = status;
    }

    public ErrorResponse(String error, String message, int status, String path) {
        this(error, message, status);
        this.path = path;
    }

    /**
     * Field-level validation detail for a request payload.
     */
    @Getter
    @Setter
    @Schema(description = "Validation error associated with a single request field.")
    public static class FieldError{
        @Schema(description = "Name of the field that failed validation.", example = "username")
        private String field;

        @Schema(description = "Rejected value provided by the client.", example = "")
        private Object rejectedValue;

        @Schema(description = "Validation message for the field.", example = "Username is required")
        private String message;

        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

    }
}

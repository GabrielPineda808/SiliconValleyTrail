package com.example.game.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String error;
    private String message;
    private int status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;
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

    @Getter
    @Setter
    public static class FieldError{
        private String field;
        private Object rejectedValue;
        private String message;

        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

    }
}

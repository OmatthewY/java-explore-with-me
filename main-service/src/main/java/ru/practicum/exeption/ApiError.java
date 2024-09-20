package ru.practicum.exeption;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ApiError {
    private String status;
    private String reason;
    private String message;
    private List<String> errors;
    private String timestamp;


    public ApiError(String status, String reason, String message, LocalDateTime timestamp) {
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.timestamp = timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public ApiError(String status, String reason, String message, List<String> errors, LocalDateTime timestamp) {
        this(status, reason, message, timestamp);
        this.errors = errors;
    }
}

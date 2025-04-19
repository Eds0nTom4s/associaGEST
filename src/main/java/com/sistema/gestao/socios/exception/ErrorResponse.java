package com.sistema.gestao.socios.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // Use modern date/time API

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error; // e.g., "Not Found", "Bad Request"
    private String message;
    private String path; // The request path where the error occurred
}

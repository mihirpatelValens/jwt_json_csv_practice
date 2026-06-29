package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponseDTO {

    private String message;
    private int statusCode;
    private LocalDateTime timeStamp;
    private String path;

    public ErrorResponseDTO(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
        this.timeStamp = LocalDateTime.now();
    }

    public ErrorResponseDTO(String message, int statusCode, String path) {
        this.message = message;
        this.statusCode = statusCode;
        this.timeStamp = LocalDateTime.now();
        this.path = path;
    }
}
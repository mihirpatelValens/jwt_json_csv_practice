package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.dto.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNameNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNameNotFound(UserNameNotFoundException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(ex.getMessage(), ex.getStatus());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}

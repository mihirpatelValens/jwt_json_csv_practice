package com.example.demo.exception;

public class FileNotFoundForUserException extends RuntimeException {
    public FileNotFoundForUserException(String message) {
        super(message);
    }
}
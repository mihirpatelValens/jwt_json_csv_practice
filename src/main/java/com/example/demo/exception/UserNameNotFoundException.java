package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNameNotFoundException extends RuntimeException {
    private final int status;

    public UserNameNotFoundException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus(){
        return status;
    }
}

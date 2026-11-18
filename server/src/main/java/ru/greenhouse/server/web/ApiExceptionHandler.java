package ru.greenhouse.server.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Dto.Error badRequest(IllegalArgumentException e) {
        return new Dto.Error(e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Dto.Error unauthorized(SecurityException e) {
        return new Dto.Error(e.getMessage());
    }
}

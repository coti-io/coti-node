package io.coti.cotinode.controllers;

import io.coti.cotinode.http.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static io.coti.cotinode.http.HttpStringConstants.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandlerControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleArgumentNotValid(MethodArgumentNotValidException e) {
        log.debug("Received a request with missing parameters.",e);
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(INVALID_PARAMETERS_MESSAGE, API_CLIENT_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }


    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity handleNullPointerException(Exception e) {
        log.error("Unhandled exception raised for the given request.");
        e.printStackTrace();
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(INNER_EXCEPTION_MESSAGE, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity handleDefaultException(Exception e) {
        log.error("Unhandled exception raised for the given request.");
        e.printStackTrace();
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(INNER_EXCEPTION_MESSAGE, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);

        return responseEntity;
    }
}
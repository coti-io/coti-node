package io.coti.basenode.exceptions;

import io.coti.basenode.http.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleArgumentNotValid(MethodArgumentNotValidException e) {
        log.info("Received a request with missing parameters.");
        log.info("Exception message: " + e);
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(INVALID_PARAMETERS_MESSAGE, API_CLIENT_ERROR), HttpStatus.BAD_REQUEST);
        return responseEntity;
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity handleNullPointerException(NullPointerException e) {
        log.error("NullPointerException for a request.");
        log.error("Exception: ", e);
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(GENERAL_EXCEPTION_ERROR, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.info("Received a request with missing parameters.");
        log.info("Exception message: " + e);
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(INVALID_PARAMETERS_MESSAGE, API_CLIENT_ERROR), HttpStatus.BAD_REQUEST);
        return responseEntity;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleDefaultException(Exception e) {
        log.error("{} for a request.", e.getClass().getSimpleName());
        log.error("Exception: ", e);
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(GENERAL_EXCEPTION_ERROR, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity handleTransactionException(TransactionException e) {
        log.error("An error while adding transaction, performing a rollback procedure", e);
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(STATUS_ERROR, TRANSACTION_ROLLBACK_MESSAGE), HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }
}
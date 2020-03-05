package io.coti.basenode.exceptions;

import io.coti.basenode.http.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;
import javax.validation.UnexpectedTypeException;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String EXCEPTION_MESSAGE = "Exception message: ";

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, UnexpectedTypeException.class})
    public ResponseEntity<ExceptionResponse> handleArgumentNotValid(Exception e) {
        log.info("Received a request with missing or invalid parameters.");
        log.info(EXCEPTION_MESSAGE + e);
        return new ResponseEntity<>(
                new ExceptionResponse(INVALID_PARAMETERS_MESSAGE, API_CLIENT_ERROR), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ExceptionResponse> handleNullPointerException(NullPointerException e) {
        log.error("NullPointerException for a request.");
        log.error("Exception: ", e);
        return new ResponseEntity<>(
                new ExceptionResponse(GENERAL_EXCEPTION_ERROR, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.info("Received a request with unsupported method");
        log.info(EXCEPTION_MESSAGE + e.getMessage());
        return new ResponseEntity<>(
                new ExceptionResponse(METHOD_NOT_SUPPORTED, API_CLIENT_ERROR), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.info("Received a request with no handler");
        log.info(EXCEPTION_MESSAGE + e.getMessage());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(responseHeaders).body(new ExceptionResponse(e.getMessage(), API_CLIENT_ERROR));
    }

    @ExceptionHandler(RequestRejectedException.class)
    public ResponseEntity<ExceptionResponse> handleRequestRejectedException(RequestRejectedException e) {
        log.info("Received a rejected request");
        log.info(EXCEPTION_MESSAGE + e.getMessage());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(responseHeaders).body(new ExceptionResponse(e.getMessage(), API_CLIENT_ERROR));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.info("Received a request with missing parameters.");
        log.info(EXCEPTION_MESSAGE + e);
        return new ResponseEntity<>(
                new ExceptionResponse(INVALID_PARAMETERS_MESSAGE, API_CLIENT_ERROR), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException e) {
        log.info("Client aborted");
        log.info("Exception: {}", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleDefaultException(Exception e) {
        log.error("{} for a request.", e.getClass().getSimpleName());
        log.error("Exception: " + e);
        return new ResponseEntity<>(
                new ExceptionResponse(GENERAL_EXCEPTION_ERROR, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ExceptionResponse> handleTransactionException(TransactionException e) {
        log.error("An error while adding transaction, performing a rollback procedure. Exception message: {}", e.getMessage());
        return new ResponseEntity<>(
                new ExceptionResponse(TRANSACTION_ROLLBACK_MESSAGE, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
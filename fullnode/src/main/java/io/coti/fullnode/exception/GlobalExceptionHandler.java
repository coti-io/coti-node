package io.coti.fullnode.exception;

import io.coti.common.http.AddTransactionResponse;
import io.coti.common.http.ExceptionResponse;
import io.coti.fullnode.service.interfaces.IBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static io.coti.common.http.HttpStringConstants.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private IBalanceService balanceService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleArgumentNotValid(MethodArgumentNotValidException e) {
        log.debug("Received a request with missing parameters.",e);
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(INVALID_PARAMETERS_MESSAGE, API_CLIENT_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }


    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity handleNullPointerException(NullPointerException e) {
        log.error("Unhandled io.coti.fullnode.exception raised for the given request.");
        e.printStackTrace();
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(INNER_EXCEPTION_MESSAGE, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity handleDefaultException(Exception e) {
        log.error("Unhandled io.coti.fullnode.exception raised for the given request.");
        e.printStackTrace();
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(INNER_EXCEPTION_MESSAGE, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);

        return responseEntity;
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity handleTransactionException(TransactionException e) {
        log.error("An error while adding transaction, performing a rollback procedure",e);
        balanceService.rollbackBaseTransactions(e.getBaseTransactionData());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AddTransactionResponse(
                        STATUS_ERROR,
                        TRANSACTION_ROLLBACK_MESSAGE));
    }




}
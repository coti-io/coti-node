package io.coti.fullnode.exception;

import com.sun.javaws.exceptions.InvalidArgumentException;
import io.coti.common.exceptions.TransactionException;
import io.coti.common.http.AddTransactionResponse;
import io.coti.common.http.ExceptionResponse;
import io.coti.common.services.interfaces.IBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

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
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(GENERAL_EXCEPTION_ERROR, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity handleDefaultException(Exception e)
    {
        log.error("Unhandled io.coti.fullnode.exception raised for the given request.",e);
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(GENERAL_EXCEPTION_ERROR, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);

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



    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity handleInvalidArgumentException(InvalidArgumentException e) {
        log.error("Unhandled io.coti.fullnode.exception raised for the given request. invalid argument");
        ResponseEntity responseEntity = new ResponseEntity(
                new ExceptionResponse(INVALID_PARAMETERS_MESSAGE, API_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity;
    }

}
package io.coti.basenode.exceptions;

public class CurrencyException extends CotiRunTimeException {

    public CurrencyException(String message) {
        super(message);
    }

    public CurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
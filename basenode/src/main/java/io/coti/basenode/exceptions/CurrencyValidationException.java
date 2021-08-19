package io.coti.basenode.exceptions;

public class CurrencyValidationException extends CotiRunTimeException {

    public CurrencyValidationException(String message) {
        super(message);
    }

    public CurrencyValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package io.coti.basenode.exceptions;

public class BalanceException extends CotiRunTimeException {

    public BalanceException(String message) {
        super(message);
    }

    public BalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}

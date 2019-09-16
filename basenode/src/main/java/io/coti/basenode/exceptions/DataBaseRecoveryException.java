package io.coti.basenode.exceptions;

public class DataBaseRecoveryException extends CotiRunTimeException {

    public DataBaseRecoveryException(String message) {
        super(message);
    }

    public DataBaseRecoveryException(String message, Throwable cause) {
        super(message, cause);
    }

}

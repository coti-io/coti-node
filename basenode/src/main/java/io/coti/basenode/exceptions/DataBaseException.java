package io.coti.basenode.exceptions;

public class DataBaseException extends CotiRunTimeException {

    public DataBaseException(String message) {
        super(message);
    }

    public DataBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

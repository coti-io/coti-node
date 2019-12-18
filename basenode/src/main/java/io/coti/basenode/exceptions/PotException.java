package io.coti.basenode.exceptions;

public class PotException extends CotiRunTimeException {

    public PotException(String message) {
        super(message);
    }

    public PotException(String message, Throwable cause) {
        super(message, cause);
    }
}

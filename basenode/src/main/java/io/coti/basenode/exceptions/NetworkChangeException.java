package io.coti.basenode.exceptions;

public class NetworkChangeException extends CotiRunTimeException {

    public NetworkChangeException(String message) {
        super(message);
    }

    public NetworkChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}

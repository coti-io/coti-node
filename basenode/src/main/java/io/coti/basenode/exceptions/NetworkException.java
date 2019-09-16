package io.coti.basenode.exceptions;

public class NetworkException extends CotiRunTimeException {

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}

package io.coti.basenode.exceptions;

public class NetworkNodeValidationException extends CotiRunTimeException {

    public NetworkNodeValidationException(String message) {
        super(message);
    }

    public NetworkNodeValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}

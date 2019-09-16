package io.coti.basenode.exceptions;

public class AwsException extends CotiRunTimeException {

    public AwsException(String message) {
        super(message);
    }

    public AwsException(String message, Throwable cause) {
        super(message, cause);
    }
}

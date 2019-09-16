package io.coti.basenode.exceptions;

public class AwsDataTransferException extends CotiRunTimeException {

    public AwsDataTransferException(String message) {
        super(message);
    }

    public AwsDataTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}

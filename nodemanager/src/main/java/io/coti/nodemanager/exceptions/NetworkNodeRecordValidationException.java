package io.coti.nodemanager.exceptions;

import io.coti.basenode.exceptions.CotiRunTimeException;

public class NetworkNodeRecordValidationException extends CotiRunTimeException {

    public NetworkNodeRecordValidationException(String message) {
        super(message);
    }

    public NetworkNodeRecordValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package io.coti.nodemanager.exceptions;

import io.coti.basenode.exceptions.CotiRunTimeException;

public class NetworkHistoryValidationException extends CotiRunTimeException {

    public NetworkHistoryValidationException(String message) {
        super(message);
    }
}

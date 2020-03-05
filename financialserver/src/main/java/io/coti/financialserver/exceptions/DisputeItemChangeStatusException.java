package io.coti.financialserver.exceptions;

import io.coti.basenode.exceptions.CotiRunTimeException;

public class DisputeItemChangeStatusException extends CotiRunTimeException {

    public DisputeItemChangeStatusException(String message) {
        super(message);
    }
}

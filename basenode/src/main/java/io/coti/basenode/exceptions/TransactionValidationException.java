package io.coti.basenode.exceptions;

import javax.validation.ValidationException;

public class TransactionValidationException extends ValidationException {

    public TransactionValidationException(String message) {
        super(message);
    }
}

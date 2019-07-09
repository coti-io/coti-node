package io.coti.basenode.exceptions;

import javax.validation.ValidationException;

public class NetworkNodeValidationException extends ValidationException {

    public NetworkNodeValidationException(String message) {
        super(message);
    }
}

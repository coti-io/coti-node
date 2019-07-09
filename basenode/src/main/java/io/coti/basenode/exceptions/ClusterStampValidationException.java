package io.coti.basenode.exceptions;

import javax.validation.ValidationException;

public class ClusterStampValidationException extends ValidationException {

    public ClusterStampValidationException(String message) {
        super(message);
    }
}

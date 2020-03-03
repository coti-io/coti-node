package io.coti.trustscore.exceptions;

import io.coti.basenode.exceptions.CotiRunTimeException;

public class BucketBuilderException extends CotiRunTimeException {

    public BucketBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}

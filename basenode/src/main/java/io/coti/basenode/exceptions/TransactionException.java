package io.coti.basenode.exceptions;

public class TransactionException extends RuntimeException {

    public TransactionException(Exception e) {
        super(e);
    }
}


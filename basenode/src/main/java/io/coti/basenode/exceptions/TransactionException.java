package io.coti.basenode.exceptions;

public class TransactionException extends RuntimeException {
    public TransactionException() {
        super();
    }

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(Exception ex) {
        super(ex);
    }

    public TransactionException(String message, Exception ex) {
        super(message, ex);
    }
}


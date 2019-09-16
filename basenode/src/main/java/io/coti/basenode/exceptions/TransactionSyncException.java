package io.coti.basenode.exceptions;

public class TransactionSyncException extends CotiRunTimeException {

    public TransactionSyncException(String message) {
        super(message);
    }

    public TransactionSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}

package io.coti.basenode.exceptions;

public class ClusterStampValidationException extends ClusterStampException {

    public ClusterStampValidationException(String message) {
        super(message);
    }

    public ClusterStampValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

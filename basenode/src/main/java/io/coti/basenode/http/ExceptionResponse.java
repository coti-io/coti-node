package io.coti.basenode.http;

public class ExceptionResponse extends Response {
    public String type;

    public ExceptionResponse(String message, String errorType) {
        super(message);
        this.type = errorType;
        this.status = BaseNodeHttpStringConstants.STATUS_ERROR;
    }
}

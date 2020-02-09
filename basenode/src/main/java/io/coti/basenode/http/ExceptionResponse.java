package io.coti.basenode.http;

import lombok.Data;

@Data
public class ExceptionResponse extends Response {

    private String type;

    public ExceptionResponse(String message, String errorType) {
        super(message);
        this.type = errorType;
        this.status = BaseNodeHttpStringConstants.STATUS_ERROR;
    }
}

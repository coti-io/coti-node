package io.coti.basenode.http;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExceptionResponse extends Response {

    private String type;
    private String details;

    public ExceptionResponse(String message, String errorType) {
        super(message);
        this.type = errorType;
        this.status = BaseNodeHttpStringConstants.STATUS_ERROR;
        this.details = "None";
    }

    public ExceptionResponse(String message, String errorType, String details) {
        super(message);
        this.type = errorType;
        this.status = BaseNodeHttpStringConstants.STATUS_ERROR;
        this.details = details;
    }
}

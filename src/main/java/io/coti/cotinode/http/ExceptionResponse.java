package io.coti.cotinode.http;

import static io.coti.cotinode.http.HttpStringConstants.STATUS_ERROR;

public class ExceptionResponse extends Response{
    public String type;

    public ExceptionResponse(String message, String errorType){
        super(message);
        this.type = errorType;
        this.status = STATUS_ERROR;
    }
}

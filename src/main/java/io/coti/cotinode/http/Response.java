package io.coti.cotinode.http;

import io.coti.cotinode.http.interfaces.IResponse;
import lombok.Data;

import static io.coti.cotinode.http.HttpStringConstants.STATUS_SUCCESS;

@Data
public abstract class Response implements IResponse {
    public String status;
    public String message;

    public Response(){
        this("");
    }

    public Response(String message) {
        this.status = STATUS_SUCCESS;
        this.message = message;
    }

    public Response(String message, String status) {
        this.status = status;
        this.message = message;
    }
}

package io.coti.common.http;

import io.coti.common.http.interfaces.IResponse;
import lombok.Data;

@Data
public abstract class Response implements IResponse {
    public String status;
    public String message;

    public Response(){
        this("");
    }

    public Response(String message) {
        this.status = HttpStringConstants.STATUS_SUCCESS;
        this.message = message;
    }

    public Response(String message, String status) {
        this.status = status;
        this.message = message;
    }
}

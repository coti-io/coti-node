package io.coti.cotinode.http;

import io.coti.cotinode.http.interfaces.IResponse;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public abstract class Response implements IResponse {
    public String message;
    public HttpStatus status;

    public Response(HttpStatus status, String message){
        this.status = status;
        this.message = message;
    }

}

package io.coti.basenode.http;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Response extends BaseResponse {

    protected String message;

    public Response() {
        this("");
    }

    public Response(String message) {
        this.message = message;
    }

    public Response(String message, String status) {
        super(status);
        this.message = message;
    }
}
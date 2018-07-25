package io.coti.common.http;


import lombok.Data;


@Data
public abstract class Response extends BaseResponse {
    public String status;
    public String message;

    public Response(){
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

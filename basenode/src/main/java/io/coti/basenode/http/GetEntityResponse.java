package io.coti.basenode.http;

import lombok.Data;

@Data
public class GetEntityResponse extends Response {

    private String objectAsJson;

    public GetEntityResponse(String message, String status, String objectAsJson) {
        super(message, status);
        this.objectAsJson = objectAsJson;
    }

    public GetEntityResponse(String objectAsJson) {
        this.objectAsJson = objectAsJson;
    }

    public GetEntityResponse(String message, String objectAsJson) {
        super(message);
        this.objectAsJson = objectAsJson;
    }
}

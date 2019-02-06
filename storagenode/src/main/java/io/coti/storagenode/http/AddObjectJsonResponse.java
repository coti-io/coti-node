package io.coti.storagenode.http;

import io.coti.basenode.http.Response;
import lombok.Data;

@Data
public class AddObjectJsonResponse extends Response {
    private String details;

    public AddObjectJsonResponse(String status, String message, String details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}

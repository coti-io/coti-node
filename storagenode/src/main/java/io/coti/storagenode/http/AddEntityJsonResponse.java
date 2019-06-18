package io.coti.storagenode.http;

import io.coti.basenode.http.Response;
import lombok.Data;

@Data
public class AddEntityJsonResponse extends Response {
    private String details;

    public AddEntityJsonResponse(String status, String message, String details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}

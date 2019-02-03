package io.coti.storagenode.http;

import io.coti.basenode.http.Response;

public class AddObjectJsonResponse extends Response {
    public String details;

    public AddObjectJsonResponse(String status, String message, String details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}

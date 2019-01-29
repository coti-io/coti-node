package io.coti.historynode.http;

import io.coti.basenode.http.Response;

public class AddAddressJsonResponse extends Response {
    public String details;
    public AddAddressJsonResponse(String status, String message, String  details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}

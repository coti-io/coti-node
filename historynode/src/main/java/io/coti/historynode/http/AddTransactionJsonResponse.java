package io.coti.historynode.http;

import io.coti.basenode.http.Response;

public class AddTransactionJsonResponse extends Response {
    public String details;
    public AddTransactionJsonResponse(String status, String message, String  details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}

package io.coti.fullnode.http;

import io.coti.basenode.http.Response;

public class AddTransactionResponse extends Response {
    public AddTransactionResponse(String status, String message) {
        super(message);
        this.status = status;
    }
}
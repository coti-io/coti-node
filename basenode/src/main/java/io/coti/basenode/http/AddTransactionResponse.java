package io.coti.basenode.http;

public class AddTransactionResponse extends Response {
    public AddTransactionResponse(String status, String message) {
        super(message);
        this.status = status;
    }
}
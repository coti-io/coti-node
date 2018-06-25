package io.coti.cotinode.http;

public class AddTransactionResponse extends Response {
    public AddTransactionResponse(String status, String message) {
        super(message);
        this.status = status;
    }
}

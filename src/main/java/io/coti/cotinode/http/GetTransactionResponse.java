package io.coti.cotinode.http;

public class GetTransactionResponse extends Response {
    public GetTransactionResponse(){
        this("TransactionResponse");
    }
    public GetTransactionResponse(String message) {
        super(message);
    }
}

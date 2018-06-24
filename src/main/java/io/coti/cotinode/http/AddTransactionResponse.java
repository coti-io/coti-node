package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;

import static io.coti.cotinode.http.HttpStringConstants.*;

public class AddTransactionResponse extends Response {
    public AddTransactionResponse(Hash transactionHash, String status) {
        super(String.format(TRANSACTION_CREATED_MESSAGE, transactionHash.toHexString()));
        this.status = status;
        if (status == STATUS_ERROR) {
            this.message = String.format(TRANSACTION_FAILED_MESSAGE, transactionHash.toHexString());
        }
    }
}

package io.coti.common.http;

import io.coti.common.data.TransactionData;
import lombok.Data;

@Data
public class GetTransactionResponse extends Response {
    private TransactionData transactionData;

    public GetTransactionResponse(TransactionData transactionData) {
        super();
        this.transactionData = transactionData;
    }
}


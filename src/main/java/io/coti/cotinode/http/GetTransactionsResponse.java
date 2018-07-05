package io.coti.cotinode.http;


import io.coti.cotinode.data.TransactionData;

import java.util.List;

public class GetTransactionsResponse extends Response {
    private List<TransactionData> transactionsData;

    public GetTransactionsResponse(List<TransactionData> transactionsData) {
        super();
        this.transactionsData = transactionsData;
    }
}
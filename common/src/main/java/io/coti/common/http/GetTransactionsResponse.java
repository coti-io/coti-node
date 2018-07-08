package io.coti.common.http;


import io.coti.common.data.TransactionData;

import java.util.List;

public class GetTransactionsResponse extends Response {
    private List<TransactionData> transactionsData;

    public GetTransactionsResponse(List<TransactionData> transactionsData) {
        super();
        this.transactionsData = transactionsData;
    }
}
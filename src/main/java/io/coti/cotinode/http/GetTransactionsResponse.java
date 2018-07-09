package io.coti.cotinode.http;


import io.coti.cotinode.data.TransactionData;
import lombok.Data;

import java.util.List;

@Data
public class GetTransactionsResponse extends Response {
    private List<TransactionData> transactionsData;

    private int lastIndex;

    public GetTransactionsResponse(List<TransactionData> transactionsData) {
        super();
        this.transactionsData = transactionsData;
    }
}
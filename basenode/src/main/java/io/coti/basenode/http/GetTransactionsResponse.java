package io.coti.basenode.http;


import io.coti.basenode.data.TransactionData;
import lombok.Data;

import java.util.List;
@Data
public class GetTransactionsResponse extends Response {
    private List<TransactionData> transactionsData;

    public GetTransactionsResponse(List<TransactionData> transactionsData) {
        super();
        this.transactionsData = transactionsData;
    }
}
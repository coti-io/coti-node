package io.coti.basenode.http;

import io.coti.basenode.data.TransactionData;
import lombok.Data;

import java.util.List;

@Data
public class GetTransactionBatchResponse {
    List<TransactionData> transactions;

    public GetTransactionBatchResponse(List<TransactionData> transactions) {
        this.transactions = transactions;
    }

    public GetTransactionBatchResponse() {
    }
}

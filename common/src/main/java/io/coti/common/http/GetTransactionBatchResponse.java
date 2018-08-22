package io.coti.common.http;

import io.coti.common.data.TransactionData;
import io.coti.common.http.data.TransactionResponseData;
import lombok.Data;

import java.util.List;

@Data
public class GetTransactionBatchResponse {
    List<TransactionData> transactions;

    public GetTransactionBatchResponse(List<TransactionData> transactions) {
        this.transactions = transactions;
    }

    public GetTransactionBatchResponse(){}
}

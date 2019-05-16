package io.coti.basenode.http;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.interfaces.ISerializable;
import lombok.Data;

import java.util.List;

@Data
public class GetTransactionBatchResponse implements ISerializable {
    List<TransactionData> transactions;

    public GetTransactionBatchResponse(List<TransactionData> transactions) {
        this.transactions = transactions;
    }

    public GetTransactionBatchResponse() {
    }
}

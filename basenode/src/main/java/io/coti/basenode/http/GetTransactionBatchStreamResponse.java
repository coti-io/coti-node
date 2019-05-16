package io.coti.basenode.http;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.interfaces.ISerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GetTransactionBatchStreamResponse implements ISerializable, Serializable {
    List<TransactionData> transactions;

    public GetTransactionBatchStreamResponse(List<TransactionData> transactions) {
        this.transactions = transactions;
    }

    public GetTransactionBatchStreamResponse() {
    }
}

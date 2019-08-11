package io.coti.basenode.http.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

@Data
public class GetHashToTransactionData implements IPropagatable {

    private Hash hash;
    private TransactionData transactionData;

    public GetHashToTransactionData() {
    }

    public GetHashToTransactionData(Hash hash, TransactionData transactionData) {
        this.hash = hash;
        this.transactionData = transactionData;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public TransactionData getTransactionData() {
        return transactionData;
    }
}

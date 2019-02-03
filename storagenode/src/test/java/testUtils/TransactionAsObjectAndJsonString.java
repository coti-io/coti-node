package testUtils;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import lombok.Data;

@Data
public class TransactionAsObjectAndJsonString {
    private Hash hash;
    private TransactionData transactionData;
    private String transactionAsJsonString;

    public TransactionAsObjectAndJsonString(Hash hash, TransactionData transactionData, String transactionAsJsonString) {
        this.hash = hash;
        this.transactionData = transactionData;
        this.transactionAsJsonString = transactionAsJsonString;
    }
}

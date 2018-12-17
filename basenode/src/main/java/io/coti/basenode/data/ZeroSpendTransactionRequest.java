package io.coti.basenode.data;


import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

@Data
public class ZeroSpendTransactionRequest implements IPropagatable {

    private Hash hash;
    private TransactionData transactionData;

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }
}

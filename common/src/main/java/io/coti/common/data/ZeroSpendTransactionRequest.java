package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

@Data
public class ZeroSpendTransactionRequest implements IEntity {

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

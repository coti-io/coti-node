package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;

@Data
public class LastTransactionData implements IEntity {
    private Hash transactionHash;
    private Date indexingTime;

    @Override
    public Hash getHash() {
        return this.transactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionHash = hash;
    }
}

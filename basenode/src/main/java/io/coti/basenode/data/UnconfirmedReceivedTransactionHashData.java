package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.time.Instant;

@Data
public class UnconfirmedReceivedTransactionHashData implements IEntity {

    private static final long serialVersionUID = 231411106093463531L;
    private Hash transactionHash;
    private Instant createdTime;
    private int retries;

    public UnconfirmedReceivedTransactionHashData(Hash transactionHash, int retries) {
        this.transactionHash = transactionHash;
        this.createdTime = Instant.now();
        this.retries = retries;
    }

    @Override
    public Hash getHash() {
        return this.transactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionHash = hash;
    }
}

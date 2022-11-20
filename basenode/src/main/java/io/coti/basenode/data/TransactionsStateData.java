package io.coti.basenode.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

import java.time.Instant;

@Data
public class TransactionsStateData implements IPropagatable {

    private static final long serialVersionUID = -6934272186683108865L;
    private long transactionsAmount;
    private Instant creationTime;
    private transient Hash hash;

    public TransactionsStateData() {
    }

    public TransactionsStateData(long transactionsAmount) {
        this.transactionsAmount = transactionsAmount;
        this.creationTime = Instant.now();
        this.hash = CryptoHelper.cryptoHash(this.creationTime.toString().getBytes());
    }

    public TransactionsStateData(long transactionsAmount, Instant creationTime, Hash hash) {
        this.transactionsAmount = transactionsAmount;
        this.creationTime = creationTime;
        this.hash = hash;
    }

    @Override
    public Hash getHash() {
        return this.hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }
}

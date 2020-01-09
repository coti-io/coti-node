package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class UnconfirmedReceivedTransactionHashData implements IEntity {

    private static final long serialVersionUID = 231411106093463531L;
    @NotNull
    private Hash transactionHash;
    @NotNull
    private Instant createdTime;

    public UnconfirmedReceivedTransactionHashData(@NotNull Hash transactionHash) {
        this.transactionHash = transactionHash;
        this.createdTime = Instant.now();
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

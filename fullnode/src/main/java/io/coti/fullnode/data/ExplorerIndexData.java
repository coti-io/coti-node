package io.coti.fullnode.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class ExplorerIndexData implements IEntity {

    private Hash transactionHash;
    private long explorerIndex;

    public ExplorerIndexData(long explorerIndex, Hash transactionHash) {
        this.explorerIndex = explorerIndex;
        this.transactionHash = transactionHash;
    }

    @Override
    public Hash getHash() {
        return new Hash(explorerIndex);
    }

    @Override
    public void setHash(Hash hash) {
    }
}

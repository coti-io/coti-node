package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;

import java.util.List;

public class RecourseClaimData implements IEntity {
    private Hash merchantHash;
    private List<Hash> disputes;

    @Override
    public Hash getHash() {
        return merchantHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.merchantHash = hash;
    }
}

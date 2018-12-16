package io.coti.financialserver.data;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;

@Data
public class UserDisputesData implements IEntity {
    private Hash userHash;
    private List<Hash> disputeHashes;

    public void appendDisputeHash(Hash disputeHash) {
        if(disputeHashes == null) {
            disputeHashes = new ArrayList<>();
        }
        disputeHashes.add(disputeHash);
    }

    @Override
    public Hash getHash() {
        return userHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.userHash = hash;
    }
}

package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserDisputeEventData implements IEntity{

    private Hash userHash;
    private List<Hash> disputeEventHashes;

    public UserDisputeEventData(Hash userHash) {
        this.userHash = userHash;
        disputeEventHashes = new ArrayList<>();
    }

    @Override
    public Hash getHash() {
        return userHash;
    }

    @Override
    public void setHash(Hash hash) {
        userHash = hash;
    }
}

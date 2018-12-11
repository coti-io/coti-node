package io.coti.financialserver.data;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;

@Data
public class MerchantDisputesData implements IEntity {
    private Hash merchantHash;
    private List<Hash> disputeHashes;

    public void appendDisputeHash(Hash disputeHash) {
        if(disputeHashes == null) {
            disputeHashes = new ArrayList<>();
        }
        disputeHashes.add(disputeHash);
    }

    @Override
    public Hash getHash() {
        return merchantHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.merchantHash = hash;
    }
}

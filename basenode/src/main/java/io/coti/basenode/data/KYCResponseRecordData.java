package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.http.data.KYCApprovementResponse;
import lombok.Data;

@Data
public class KYCResponseRecordData implements IEntity {

    private Hash nodeHash;

    private KYCApprovementResponse kycApprovementResponse;

    private boolean valid;


    @Override
    public Hash getHash() {
        return nodeHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.nodeHash = hash;
    }
}

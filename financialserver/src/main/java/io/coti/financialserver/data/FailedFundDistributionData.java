package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FailedFundDistributionData implements IEntity {

    private static final long serialVersionUID = 2089169974713456690L;
    private Hash hash;
    private Map<Hash, Hash> fundDistributionHashes;

    public FailedFundDistributionData(Hash hash) {
        this.hash = hash;
        this.fundDistributionHashes = new HashMap<>();
    }
}

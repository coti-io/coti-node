package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;

@Data
public class FailedFundDistributionData implements IEntity {

    @NotEmpty
    protected Hash hash;
    protected Map<Hash,Hash> fundDistributionHashes;

    public FailedFundDistributionData(Hash hash) {
        this.hash = hash;
        this.fundDistributionHashes = new HashMap<>();
    }
}

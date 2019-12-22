package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;

@Data
public class FailedFundDistributionData implements IEntity {

    private static final long serialVersionUID = 2089169974713456690L;
    @NotEmpty
    protected Hash hash;
    private Map<Hash, Hash> fundDistributionHashes;

    public FailedFundDistributionData() {
    }

    public FailedFundDistributionData(Hash hash) {
        this.hash = hash;
        this.fundDistributionHashes = new HashMap<>();
    }
}

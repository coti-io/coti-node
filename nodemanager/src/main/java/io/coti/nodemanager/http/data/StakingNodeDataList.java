package io.coti.nodemanager.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StakingNodeDataList {
    private String node;

    public StakingNodeDataList(Hash nodeHash, BigDecimal stake) {
        this.node = "Node " + nodeHash.toHexString() + " stake " + stake.toString();
    }
}

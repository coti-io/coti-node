package io.coti.nodemanager.http.data;

import io.coti.nodemanager.data.StakingNodeData;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StakingNodeResponseData implements Serializable {

    private BigDecimal stake;
    private String nodeHash;

    public StakingNodeResponseData(StakingNodeData stakingNodeData) {
        stake = stakingNodeData.getStake();
        nodeHash = stakingNodeData.getNodeHash().toString();
    }
}

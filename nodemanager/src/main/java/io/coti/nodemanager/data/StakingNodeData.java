package io.coti.nodemanager.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class StakingNodeData implements IEntity {

    private static final long serialVersionUID = 2150755557556320091L;
    @NotNull
    private @Valid Hash nodeHash;
    @DecimalMin(value = "0")
    private BigDecimal stake;

    private StakingNodeData() {
    }

    public StakingNodeData(Hash nodeHash, BigDecimal stake) {
        this.nodeHash = nodeHash;
        this.stake = stake;
    }

    @Override
    public Hash getHash() {
        return nodeHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.nodeHash = hash;
    }
}

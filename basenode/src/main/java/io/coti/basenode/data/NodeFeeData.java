package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class NodeFeeData implements IEntity {

    private static final long serialVersionUID = 421317297863257349L;
    private Hash feeHash;
    private NodeFeeType nodeFeeType;
    private FeeData feeData;

    public NodeFeeData(NodeFeeType nodeFeeType, FeeData feeData) {
        this.nodeFeeType = nodeFeeType;
        this.feeData = feeData;
        this.feeHash = this.nodeFeeType.getHash();
    }

    @Override
    public Hash getHash() {
        return this.feeHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.feeHash = hash;
    }
}

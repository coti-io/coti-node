package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class InitialFundData implements IEntity {

    private static final long serialVersionUID = -8755795138816705813L;
    private Hash fundAddress;
    private Hash transactionHash;

    public InitialFundData(Hash fundAddress, Hash transactionHash) {
        this.fundAddress = fundAddress;
        this.transactionHash = transactionHash;
    }

    @Override
    public Hash getHash() {
        return fundAddress;
    }

    @Override
    public void setHash(Hash hash) {

    }
}

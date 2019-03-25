package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

@Data
public class InitialFundData implements IEntity {

    private Hash hash;
    private int fundId;
    private String fundName;
    private float fundPercentage;
    private int addressIndex;

    public InitialFundData() {

    }

    public InitialFundData(int fundId, String fundName, float fundPercentage) {
        this.fundId = fundId;
        this.fundName = fundName;
        this.fundPercentage = fundPercentage;
    }

    @Override
    public Hash getHash() {
        return new Hash(fundId);
    }
}

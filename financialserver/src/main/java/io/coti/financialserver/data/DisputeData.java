package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class DisputeData implements IEntity {
    private Hash hash;
    private Hash transactionHash;
    private Hash consumerHash;
    private Hash merchantHash;
    private List<Hash> arbitratorHashes;
    private List<DisputeItemData> disputeItems;
    private DisputeStatus disputeStatus;
    private BigDecimal amount;
    private BigDecimal chargeBackAmount;
    private Hash chargeBackTransactionHash;
    private BigDecimal recourseClaimAmount;
    private Boolean recourseClaimOpen;
    private Hash recourseClaimTransactionHash;
    private Date createTime;
    private Date closedTime;

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }
}

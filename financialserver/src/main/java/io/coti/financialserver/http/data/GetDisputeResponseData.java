package io.coti.financialserver.http.data;

import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.DisputeStatus;
import io.coti.financialserver.http.data.interfaces.IDisputeEventResponseData;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public abstract class GetDisputeResponseData implements IDisputeEventResponseData, Serializable {
    protected String hash;
    protected String transactionHash;
    protected Instant transactionCreationTime;
    private  List<DisputeItemResponseData> disputeItems;
    protected DisputeStatus disputeStatus;
    protected BigDecimal amount;
    protected BigDecimal chargeBackAmount;
    protected String chargeBackTransactionHash;
    protected Instant creationTime;
    protected Instant updateTime;
    protected Instant arbitratorsAssignTime;
    protected Instant closedTime;

    protected GetDisputeResponseData() {

    }

    public GetDisputeResponseData(DisputeData disputeData) {

        this.hash = disputeData.getHash().toString();
        this.transactionHash = disputeData.getTransactionHash().toString();
        this.transactionCreationTime = disputeData.getTransactionCreationTime();
        this.disputeItems = new ArrayList<>();
        this.disputeStatus = disputeData.getDisputeStatus();
        this.amount = disputeData.getAmount();
        this.chargeBackAmount = disputeData.getChargeBackAmount();
        this.chargeBackTransactionHash = disputeData.getChargeBackTransactionHash() != null ? disputeData.getChargeBackTransactionHash().toString() : null;
        this.creationTime = disputeData.getCreationTime();
        this.updateTime = disputeData.getUpdateTime();
        this.arbitratorsAssignTime = disputeData.getArbitratorsAssignTime();
        this.closedTime = disputeData.getClosedTime();

    }

    protected void setDisputeItems(List<DisputeItemData> disputeItems) {
        disputeItems.forEach(disputeItemData -> this.disputeItems.add(new DisputeItemResponseData(disputeItemData)));
    }
}

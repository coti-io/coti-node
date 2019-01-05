package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetDisputeResponseData {
    protected String hash;
    protected String transactionHash;
    protected String consumerHash;
    protected SignatureData consumerSignature;
    protected String merchantHash;
    protected List<DisputeItemResponseData> disputeItems;
    protected DisputeStatus disputeStatus;
    protected BigDecimal amount;
    protected BigDecimal chargeBackAmount;
    protected String chargeBackTransactionHash;
    protected BigDecimal recourseClaimAmount;
    protected Boolean recourseClaimOpen;
    protected String recourseClaimTransactionHash;
    protected Instant creationTime;
    protected Instant updateTime;
    protected Instant arbitratorsAssignTime;
    protected Instant closedTime;

    public GetDisputeResponseData(DisputeData disputeData) {

        this.hash = disputeData.getHash().toString();
        this.transactionHash = disputeData.getTransactionHash().toString();
        this.consumerHash = disputeData.getConsumerHash().toString();
        this.consumerSignature = disputeData.getSignature();
        this.merchantHash = disputeData.getMerchantHash().toString();
        this.disputeItems = new ArrayList<>();
        disputeData.getDisputeItems().forEach(disputeItemData -> disputeItems.add(new DisputeItemResponseData(disputeItemData)));
        this.disputeStatus = disputeData.getDisputeStatus();
        this.amount = disputeData.getAmount();
        this.chargeBackAmount = disputeData.getChargeBackAmount();
        this.chargeBackTransactionHash = disputeData.getChargeBackTransactionHash() != null ? disputeData.getChargeBackTransactionHash().toString() : null;
        this.creationTime = disputeData.getCreationTime();
        this.updateTime = disputeData.getUpdateTime();
        this.closedTime = disputeData.getClosedTime();
    }
}

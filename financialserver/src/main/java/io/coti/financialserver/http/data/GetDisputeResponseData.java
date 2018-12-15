package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class GetDisputeResponseData {
    private String hash;
    private String transactionHash;
    private String consumerHash;
    private SignatureData consumerSignature;
    private String merchantHash;
    private List<DisputeItemResponseData> disputeItems;
    private DisputeStatus disputeStatus;
    private BigDecimal amount;
    private BigDecimal chargeBackAmount;
    private String chargeBackTransactionHash;
    private BigDecimal recourseClaimAmount;
    private Boolean recourseClaimOpen;
    private String recourseClaimTransactionHash;
    private Date creationTime;
    private Date updateTime;
    private Date closedTime;

    public GetDisputeResponseData(DisputeData disputeData) {
        this.hash = disputeData.getHash().toString();
        this.transactionHash = disputeData.getReceiverBaseTransactionHash().toString();
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

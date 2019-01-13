package io.coti.financialserver.http.data;

import io.coti.financialserver.data.DisputeData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantDisputeResponseData extends GetDisputeResponseData {

    private String merchantHash;
    private BigDecimal recourseClaimAmount;
    private Boolean recourseClaimOpen;
    private String recourseClaimTransactionHash;

    public MerchantDisputeResponseData(DisputeData disputeData) {
        super(disputeData);
        setDisputeItems(disputeData.getDisputeItems());
        this.merchantHash = disputeData.getMerchantHash().toString();
        this.recourseClaimAmount = disputeData.getRecourseClaimAmount();
        this.recourseClaimOpen = disputeData.getRecourseClaimOpen();
        this.recourseClaimTransactionHash = disputeData.getRecourseClaimTransactionHash() != null ? disputeData.getRecourseClaimTransactionHash().toString() : null;

    }
}

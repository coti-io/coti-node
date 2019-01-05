package io.coti.financialserver.http.data;

import io.coti.financialserver.data.DisputeData;

import java.math.BigDecimal;

public class MerchantDisputeResponseData extends GetDisputeResponseData {

    private BigDecimal recourseClaimAmount;
    private Boolean recourseClaimOpen;
    private String recourseClaimTransactionHash;

    public MerchantDisputeResponseData(DisputeData disputeData) {
        super(disputeData);
        setDisputeItems(disputeData.getDisputeItems());
        this.recourseClaimAmount = disputeData.getRecourseClaimAmount();
        this.recourseClaimOpen = disputeData.getRecourseClaimOpen();
        this.recourseClaimTransactionHash = disputeData.getRecourseClaimTransactionHash().toString();

    }
}

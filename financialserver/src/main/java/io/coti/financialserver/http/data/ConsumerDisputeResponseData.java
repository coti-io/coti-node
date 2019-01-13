package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.financialserver.data.DisputeData;
import lombok.Data;

@Data
public class ConsumerDisputeResponseData extends GetDisputeResponseData {
    private String consumerHash;
    private SignatureData consumerSignature;

    public ConsumerDisputeResponseData(DisputeData disputeData) {
        super(disputeData);
        setDisputeItems(disputeData.getDisputeItems());
        this.consumerHash = disputeData.getConsumerHash().toString();
        this.consumerSignature = disputeData.getSignature();
    }
}

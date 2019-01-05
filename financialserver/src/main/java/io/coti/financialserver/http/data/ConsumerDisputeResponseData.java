package io.coti.financialserver.http.data;

import io.coti.financialserver.data.DisputeData;
import lombok.Data;

@Data
public class ConsumerDisputeResponseData extends GetDisputeResponseData {

    public ConsumerDisputeResponseData(DisputeData disputeData) {
        super(disputeData);
        setDisputeItems(disputeData.getDisputeItems());
    }
}

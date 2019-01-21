package io.coti.financialserver.http.data;

import io.coti.financialserver.data.DisputeStatus;
import io.coti.financialserver.data.DisputeStatusChangeEventData;
import io.coti.financialserver.http.data.interfaces.IDisputeEventResponseData;
import lombok.Data;

@Data
public class DisputeStatusChangeResponseData implements IDisputeEventResponseData {

    private String disputeHash;
    private DisputeStatus disputeStatus;
    private String transactionHash;

    public DisputeStatusChangeResponseData(DisputeStatusChangeEventData disputeStatusChangeEventData) {

        disputeHash = disputeStatusChangeEventData.getHash().toString();
        disputeStatus = disputeStatusChangeEventData.getDisputeStatus();
        transactionHash = disputeStatusChangeEventData.getTransactionHash().toString();
    }
}

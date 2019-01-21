package io.coti.financialserver.http.data;

import io.coti.financialserver.data.DisputeItemStatus;
import io.coti.financialserver.data.DisputeItemStatusChangeEventData;
import io.coti.financialserver.http.data.interfaces.IDisputeEventResponseData;
import lombok.Data;

@Data
public class DisputeItemStatusChangeResponseData implements IDisputeEventResponseData {

    private String disputeHash;
    private Long itemId;
    private DisputeItemStatus disputeItemStatus;
    private String transactionHash;

    public DisputeItemStatusChangeResponseData(DisputeItemStatusChangeEventData disputeItemStatusChangeEventData) {

        disputeHash = disputeItemStatusChangeEventData.getHash().toString();
        itemId = disputeItemStatusChangeEventData.getItemId();
        disputeItemStatus = disputeItemStatusChangeEventData.getDisputeItemStatus();
        transactionHash = disputeItemStatusChangeEventData.getTransactionHash().toString();
    }
}

package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeItemStatus;
import io.coti.financialserver.data.DisputeStatus;
import io.coti.financialserver.data.DisputeStatusChangedEvent;
import io.coti.financialserver.data.ItemStatusChangedEvent;
import lombok.Data;

@Data
public class ItemStatusChangedResponse extends BaseResponse {

    private String disputeHash;
    private Long itemId;
    private DisputeItemStatus disputeItemStatus;

    public ItemStatusChangedResponse(ItemStatusChangedEvent itemStatusChangedEvent) {
        super();

        disputeHash = itemStatusChangedEvent.getHash().toHexString();
        itemId = itemStatusChangedEvent.getItemId();
        disputeItemStatus = itemStatusChangedEvent.getDisputeItemStatus();
    }
}

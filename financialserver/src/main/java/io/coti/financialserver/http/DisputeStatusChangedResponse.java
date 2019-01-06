package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeStatus;
import io.coti.financialserver.data.DisputeStatusChangedEvent;
import io.coti.financialserver.http.data.GetCommentResponseData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DisputeStatusChangedResponse extends BaseResponse {

    private String disputeHash;
    private DisputeStatus disputeStatus;

    public DisputeStatusChangedResponse(DisputeStatusChangedEvent disputeStatusChangedEvent) {
        super();

        disputeHash = disputeStatusChangedEvent.getHash().toHexString();
        disputeStatus = disputeStatusChangedEvent.getDisputeStatus();
    }
}

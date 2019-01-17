package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class GetDisputeHistoryResponse extends BaseResponse {
    private List<DisputeEventResponse> disputeHistory;

    public GetDisputeHistoryResponse(List<DisputeEventResponse> disputeHistory) {
        super();

        this.disputeHistory = disputeHistory;
    }
}

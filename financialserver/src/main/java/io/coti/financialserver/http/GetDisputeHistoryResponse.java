package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.DisputeEventResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetDisputeHistoryResponse extends BaseResponse {

    private List<DisputeEventResponseData> disputeHistory;

    public GetDisputeHistoryResponse(List<DisputeEventResponseData> disputeHistory) {
        this.disputeHistory = disputeHistory;
    }
}

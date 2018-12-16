package io.coti.financialserver.http;

import lombok.Data;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.http.data.GetDisputeResponseData;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetDisputesResponse extends BaseResponse {

    private List<GetDisputeResponseData> disputesData;

    public GetDisputesResponse(List<DisputeData> disputesData) {
        super();

        this.disputesData = new ArrayList<>();
        for(DisputeData disputeData : disputesData) {
            this.disputesData.add(new GetDisputeResponseData(disputeData));
        }
    }
}

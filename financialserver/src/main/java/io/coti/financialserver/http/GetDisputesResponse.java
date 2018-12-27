package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.http.data.GetDisputeResponseData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetDisputesResponse extends BaseResponse {

    private List<GetDisputeResponseData> disputesData;

    public GetDisputesResponse(List<DisputeData> disputesData) {
        super();

        this.disputesData = new ArrayList<>();
        disputesData.forEach(disputeData -> this.disputesData.add(new GetDisputeResponseData(disputeData)));
    }
}

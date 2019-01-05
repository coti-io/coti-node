package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.http.data.GetDisputeResponseClass;
import io.coti.financialserver.http.data.GetDisputeResponseData;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetDisputesResponse extends BaseResponse {

    private List<GetDisputeResponseData> disputesData;

    public GetDisputesResponse(List<DisputeData> disputesData, ActionSide actionSide, Hash userHash) {
        super();

        this.disputesData = new ArrayList<>();
        disputesData.forEach(disputeData -> this.disputesData.add(GetDisputeResponseClass.valueOf(actionSide.toString()).getNewInstance(disputeData, userHash)));
    }
}

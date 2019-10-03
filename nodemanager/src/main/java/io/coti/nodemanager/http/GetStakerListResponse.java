package io.coti.nodemanager.http;


import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.data.StakingNodeData;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.http.data.StakingNodeDataList;
import io.coti.nodemanager.http.data.StakingNodeResponseData;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GetStakerListResponse extends BaseResponse {

    private List<StakingNodeResponseData> nodes;

    public GetStakerListResponse(List<StakingNodeResponseData> nodes) {
        this.nodes = nodes;
    }

}

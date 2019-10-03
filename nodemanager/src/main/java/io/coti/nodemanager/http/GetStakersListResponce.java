package io.coti.nodemanager.http;


import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.data.StakingNodeData;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.http.data.StakingNodeDataList;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GetStakersListResponce extends BaseResponse {
    private List<StakingNodeDataList> nodes;

    public GetStakersListResponce(List<StakingNodeDataList> nodes) {
        this.nodes = nodes;
    }

}

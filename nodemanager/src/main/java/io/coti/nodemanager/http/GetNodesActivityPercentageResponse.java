package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.http.data.NodeActivityPercentageData;
import lombok.Data;

import java.util.Map;

@Data
public class GetNodesActivityPercentageResponse extends BaseResponse {

    private Map<Hash, NodeActivityPercentageData> nodeHashToActivityPercentage;

    public GetNodesActivityPercentageResponse(Map<Hash, NodeActivityPercentageData> nodeHashToActivityPercentage) {
        this.nodeHashToActivityPercentage = nodeHashToActivityPercentage;
    }
}

package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

@Data
public class GetNodesActivityPercentageResponse extends BaseResponse {

    private Map<Hash, Pair> nodeHashToActivityPercentage;

    public GetNodesActivityPercentageResponse() {
    }

    public GetNodesActivityPercentageResponse(Map<Hash, Pair> nodeHashToActivityPercentage) {
        this.nodeHashToActivityPercentage = nodeHashToActivityPercentage;
    }
}

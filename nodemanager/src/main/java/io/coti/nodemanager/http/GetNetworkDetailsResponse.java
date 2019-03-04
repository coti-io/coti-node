package io.coti.nodemanager.http;


import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GetNetworkDetailsResponse extends BaseResponse {
    private Map<String, List<SingleNodeDetailsForWallet>> nodes;

    public GetNetworkDetailsResponse(Map<String, List<SingleNodeDetailsForWallet>> nodes) {
        this.nodes = nodes;
    }

}

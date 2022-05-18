package io.coti.basenode.http;

import io.coti.basenode.http.data.NetworkLastKnownNodesResponseData;
import lombok.Data;

@Data
public class GetNetworkLastKnownNodesResponse extends BaseResponse {

    private NetworkLastKnownNodesResponseData networkLastKnownNodesResponseData;

    public GetNetworkLastKnownNodesResponse() {
    }

    public GetNetworkLastKnownNodesResponse(NetworkLastKnownNodesResponseData networkLastKnownNodesResponseData) {
        this.networkLastKnownNodesResponseData = networkLastKnownNodesResponseData;
    }

}

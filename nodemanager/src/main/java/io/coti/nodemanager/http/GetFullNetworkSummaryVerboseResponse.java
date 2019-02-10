package io.coti.nodemanager.http;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class GetFullNetworkSummaryVerboseResponse extends BaseResponse {
    private NetworkData networkData;

    public GetFullNetworkSummaryVerboseResponse(NetworkData networkData) {
        this.networkData = networkData;
    }
}

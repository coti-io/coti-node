package io.coti.nodemanager.http;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class GetFullNetworkSummaryVerboseResponse extends BaseResponse {
    private NetworkDetails networkDetails;

    public GetFullNetworkSummaryVerboseResponse(NetworkDetails networkDetails) {
        this.networkDetails = networkDetails;
    }
}

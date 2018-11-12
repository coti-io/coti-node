package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.data.NetworkFeeResponseData;

public class NetworkFeeResponse extends BaseResponse {

    private NetworkFeeResponseData networkFeeResponseData;
    public NetworkFeeResponse(NetworkFeeResponseData fullNodeFeeResponseData) {
        super();
        this.networkFeeResponseData = fullNodeFeeResponseData;
    }


}

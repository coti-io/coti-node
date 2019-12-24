package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.data.NetworkFeeResponseData;
import lombok.Data;

@Data
public class NetworkFeeResponse extends BaseResponse {
    private NetworkFeeResponseData networkFeeData;

    public NetworkFeeResponse(NetworkFeeResponseData networkFeeResponseData) {
        super();
        this.networkFeeData = networkFeeResponseData;
    }

}

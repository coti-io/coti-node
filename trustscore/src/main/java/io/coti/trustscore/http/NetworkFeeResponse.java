package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.data.NetworkFeeResponseData;
import lombok.Data;

@Data
public class NetworkFeeResponse extends BaseResponse {

    private static final long serialVersionUID = -4983074251656926030L;
    private NetworkFeeResponseData networkFeeData;

    public NetworkFeeResponse(NetworkFeeResponseData networkFeeResponseData) {
        super();
        this.networkFeeData = networkFeeResponseData;
    }

}

package io.coti.trustscore.http;

import io.coti.basenode.http.Response;
import io.coti.trustscore.http.data.NetworkFeeResponseData;
import lombok.Data;

@Data
public class NetworkFeeResponse extends Response {

    private NetworkFeeResponseData networkFeeData;

    public NetworkFeeResponse(NetworkFeeResponseData networkFeeResponseData) {
        super();
        this.networkFeeData = networkFeeResponseData;
    }

    public NetworkFeeResponse(String status, String message) {
        super(message);
        this.status = status;
    }


}

package io.coti.trustscore.http;


import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.data.RollingReserveResponseData;

public class RollingReserveResponse extends BaseResponse {

    private RollingReserveResponseData rollingReserveResponseData;

    public RollingReserveResponse(RollingReserveResponseData fullNodeFeeResponseData) {
        super();
        this.rollingReserveResponseData = fullNodeFeeResponseData;
    }
}

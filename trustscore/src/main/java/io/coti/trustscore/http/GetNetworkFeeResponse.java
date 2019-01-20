package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class GetNetworkFeeResponse extends BaseResponse {
    private String userHash;
    private double networkFeeAmount;

    public GetNetworkFeeResponse(String userHash, double rollingReserveAmount) {
        this.userHash = userHash;
        this.networkFeeAmount = rollingReserveAmount;
    }
}
package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class GetRollingReserveResponse extends BaseResponse {
    private static final long serialVersionUID = -7325337249121950200L;
    private String userHash;
    private double rollingReserveAmount;

    public GetRollingReserveResponse(String userHash, double rollingReserveAmount) {
        this.userHash = userHash;
        this.rollingReserveAmount = rollingReserveAmount;
    }
}
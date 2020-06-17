package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetRollingReserveResponse extends BaseResponse {

    private String userHash;
    private double rollingReserveAmount;

    public GetRollingReserveResponse(String userHash, double rollingReserveAmount) {
        this.userHash = userHash;
        this.rollingReserveAmount = rollingReserveAmount;
    }
}
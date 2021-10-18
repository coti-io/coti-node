package io.coti.trustscore.http;


import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.data.RollingReserveResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RollingReserveResponse extends BaseResponse {

    private RollingReserveResponseData rollingReserveData;

    public RollingReserveResponse(RollingReserveResponseData rollingReserveResponseData) {
        super();
        this.rollingReserveData = rollingReserveResponseData;
    }
}

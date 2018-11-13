package io.coti.trustscore.http;


import io.coti.basenode.http.Response;
import io.coti.trustscore.http.data.RollingReserveResponseData;
import lombok.Data;

@Data
public class RollingReserveResponse extends Response {

    private RollingReserveResponseData rollingReserveData;

    public RollingReserveResponse(RollingReserveResponseData rollingReserveResponseData) {
        super();
        this.rollingReserveData = rollingReserveResponseData;
    }

    public RollingReserveResponse(String status, String message) {
        super(message);
        this.status = status;
    }
}

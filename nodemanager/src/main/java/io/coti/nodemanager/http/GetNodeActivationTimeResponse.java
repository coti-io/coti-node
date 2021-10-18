package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNodeActivationTimeResponse extends BaseResponse {

    private Instant activationTime;
    private Instant originalActivationTime;

    public GetNodeActivationTimeResponse(Instant activationTime, Instant originalActivationTime) {
        this.activationTime = activationTime;
        this.originalActivationTime = originalActivationTime;
    }

    public GetNodeActivationTimeResponse(Instant activationTime) {
        this.activationTime = activationTime;
    }
}

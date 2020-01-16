package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.time.Instant;

@Data
public class GetNodeActivationTimeResponse extends BaseResponse {

    private Instant activationTime;

    public GetNodeActivationTimeResponse(Instant activationTime) {
        this.activationTime = activationTime;
    }
}

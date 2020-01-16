package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetNodeActivationTimeResponse extends BaseResponse {

    private LocalDateTime activationTime;

    public GetNodeActivationTimeResponse(LocalDateTime activationTime) {
        this.activationTime = activationTime;
    }
}

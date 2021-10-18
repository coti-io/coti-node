package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNodeActivityPercentageResponse extends BaseResponse {

    private double activityPercentage;

    public GetNodeActivityPercentageResponse(double activityPercentage) {
        this.activityPercentage = activityPercentage;
    }
}

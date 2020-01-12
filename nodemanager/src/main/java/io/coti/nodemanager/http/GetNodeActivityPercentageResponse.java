package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class GetNodeActivityPercentageResponse extends BaseResponse {
    private double activityPercentage;

    public GetNodeActivityPercentageResponse(double activityPercentage) {
        this.activityPercentage = activityPercentage;
    }

    public GetNodeActivityPercentageResponse(String status, long activityPercentage) {
        super(status);
        this.activityPercentage = activityPercentage;
    }
}

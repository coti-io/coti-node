package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class GetNodeActivityPercentageResponse extends BaseResponse {
    private long activityPercentage;

    public GetNodeActivityPercentageResponse(long activityPercentage) {
        this.activityPercentage = activityPercentage;
    }

    public GetNodeActivityPercentageResponse(String status, long activityPercentage) {
        super(status);
        this.activityPercentage = activityPercentage;
    }
}

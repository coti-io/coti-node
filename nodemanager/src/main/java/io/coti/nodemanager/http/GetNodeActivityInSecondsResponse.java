package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.data.NodeActivityData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNodeActivityInSecondsResponse extends BaseResponse {

    private long activityUpTimeInSeconds;

    public GetNodeActivityInSecondsResponse(NodeActivityData nodeActivityData) {
        this.activityUpTimeInSeconds = nodeActivityData.getActivityUpTimeInSeconds();
    }
}

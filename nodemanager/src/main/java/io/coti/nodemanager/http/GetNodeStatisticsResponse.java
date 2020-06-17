package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.http.data.NodeStatisticsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNodeStatisticsResponse extends BaseResponse {
    private NodeStatisticsData nodeStatisticsData;

    public GetNodeStatisticsResponse(NodeStatisticsData nodeStatisticsData) {
        this.nodeStatisticsData = nodeStatisticsData;
    }
}

package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.http.data.NodeDailyStatisticsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNodeDailyStatisticsResponse extends BaseResponse {

    private List<NodeDailyStatisticsData> nodeStatisticsList;

    public GetNodeDailyStatisticsResponse(List<NodeDailyStatisticsData> nodeStatisticsList) {
        this.nodeStatisticsList = nodeStatisticsList;
    }
}

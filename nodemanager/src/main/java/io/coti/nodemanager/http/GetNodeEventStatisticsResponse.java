package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.http.data.NodeNetworkResponseData;
import lombok.Data;

import java.util.List;

@Data
public class GetNodeEventStatisticsResponse extends BaseResponse {
    private List<NodeNetworkResponseData> nodeEventsList;

    public GetNodeEventStatisticsResponse(List<NodeNetworkResponseData> nodeEventsList) {
        this.nodeEventsList = nodeEventsList;
    }
}

package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.http.data.NodeNetworkRecordResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNodeEventStatisticsResponse extends BaseResponse {

    private List<NodeNetworkRecordResponseData> nodeEventsList;

    public GetNodeEventStatisticsResponse(List<NodeNetworkRecordResponseData> nodeEventsList) {
        this.nodeEventsList = nodeEventsList;
    }
}

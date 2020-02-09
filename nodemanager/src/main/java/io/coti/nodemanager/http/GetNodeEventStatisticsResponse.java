package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class GetNodeEventStatisticsResponse extends BaseResponse {
    private List<Object> nodeEventsList;

    public GetNodeEventStatisticsResponse(List<Object> nodeEventsList) {
        this.nodeEventsList = nodeEventsList;
    }
}

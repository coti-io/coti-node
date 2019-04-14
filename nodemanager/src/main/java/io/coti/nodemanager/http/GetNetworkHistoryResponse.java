package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.data.NodeHistoryData;
import lombok.Data;

import java.util.List;

@Data
public class GetNetworkHistoryResponse extends BaseResponse {
    private List<NodeHistoryData> historyDataList;

    public GetNetworkHistoryResponse(List<NodeHistoryData> historyDataList) {
        this.historyDataList = historyDataList;
    }
}

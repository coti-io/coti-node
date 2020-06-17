package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.data.NodeHistoryData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNetworkHistoryResponse extends BaseResponse {
    private List<NodeHistoryData> historyDataList;

    public GetNetworkHistoryResponse(List<NodeHistoryData> historyDataList) {
        this.historyDataList = historyDataList;
    }
}

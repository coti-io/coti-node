package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.http.data.NodeNetworkRecordResponseData;
import lombok.Data;

@Data
public class GetNodeLastEventResponse extends BaseResponse {

    private NodeNetworkRecordResponseData lastEvent;

    public GetNodeLastEventResponse(NodeNetworkRecordResponseData lastEvent) {
        this.lastEvent = lastEvent;
    }
}

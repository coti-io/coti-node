package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.ActionSide;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GetUnreadEventsResponse extends BaseResponse {

    private List<DisputeEventResponse> unreadUserDisputeEvents;

    public GetUnreadEventsResponse(List<DisputeEventResponse> unreadUserDisputeEvents) {
        super();

        this.unreadUserDisputeEvents = unreadUserDisputeEvents;
    }
}

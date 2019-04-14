package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class GetUnreadEventsResponse extends BaseResponse {

    private List<DisputeEventResponse> unreadUserDisputeEvents;

    public GetUnreadEventsResponse(List<DisputeEventResponse> unreadUserDisputeEvents) {
        super();

        this.unreadUserDisputeEvents = unreadUserDisputeEvents;
    }
}

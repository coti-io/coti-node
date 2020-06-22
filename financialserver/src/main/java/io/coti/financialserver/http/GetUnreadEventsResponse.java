package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.DisputeEventResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetUnreadEventsResponse extends BaseResponse {

    private List<DisputeEventResponseData> unreadUserDisputeEvents;

    public GetUnreadEventsResponse(List<DisputeEventResponseData> unreadUserDisputeEvents) {

        this.unreadUserDisputeEvents = unreadUserDisputeEvents;
    }
}

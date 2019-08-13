package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.crypto.GetUnreadEventsCrypto;
import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.data.UnreadUserDisputeEventData;
import io.coti.financialserver.http.DisputeEventResponse;
import io.coti.financialserver.http.GetUnreadEventsRequest;
import io.coti.financialserver.http.GetUnreadEventsResponse;
import io.coti.financialserver.http.data.GetUnreadEventsData;
import io.coti.financialserver.model.DisputeEvents;
import io.coti.financialserver.model.UnreadUserDisputeEvents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.financialserver.http.HttpStringConstants.INVALID_SIGNATURE;


@Slf4j
@Service
public class EventService {

    @Autowired
    private GetUnreadEventsCrypto getUnreadEventsCrypto;
    @Autowired
    private UnreadUserDisputeEvents unreadUserDisputeEvents;
    @Autowired
    private DisputeEvents disputeEvents;

    public ResponseEntity<IResponse> getUnreadEvents(GetUnreadEventsRequest getUnreadEventsRequest) {
        GetUnreadEventsData getUnreadEventsData = getUnreadEventsRequest.getUnreadEventsData();

        if (!getUnreadEventsCrypto.verifySignature(getUnreadEventsData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        List<DisputeEventResponse> disputeEventResponses = new ArrayList<>();

        UnreadUserDisputeEventData unreadUserDisputeEventData = unreadUserDisputeEvents.getByHash(getUnreadEventsData.getUserHash());
        if (unreadUserDisputeEventData != null) {
            Map<Hash, ActionSide> disputeEventHashToEventDisplaySideMap = unreadUserDisputeEventData.getDisputeEventHashToEventDisplaySideMap();

            disputeEventHashToEventDisplaySideMap.forEach((disputeHash, eventDisplaySide) ->
                    disputeEventResponses.add(new DisputeEventResponse(disputeEvents.getByHash(disputeHash), getUnreadEventsData.getUserHash(), eventDisplaySide, false))
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(new GetUnreadEventsResponse(disputeEventResponses));
    }

}

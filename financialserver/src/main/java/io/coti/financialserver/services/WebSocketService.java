package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.financialserver.data.*;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import io.coti.financialserver.http.DisputeEventResponse;
import io.coti.financialserver.http.DisputeEventReadRequest;
import io.coti.financialserver.http.DisputePopulateEventsRequest;
import io.coti.financialserver.model.DisputeEvents;
import io.coti.financialserver.model.DisputeHistory;
import io.coti.financialserver.model.UnreadUserDisputeEvents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static io.coti.financialserver.http.HttpStringConstants.SUCCESS;

@Slf4j
@Service
public class WebSocketService {

    @Autowired
    private UnreadUserDisputeEvents unreadUserDisputeEvents;
    @Autowired
    private DisputeEvents disputeEvents;
    @Autowired
    private DisputeHistory disputeHistory;
    @Autowired
    private SimpMessagingTemplate messagingSender;

    public ResponseEntity eventRead(DisputeEventReadRequest disputeEventReadRequest) {

        UnreadUserDisputeEventData unreadUserDisputeEventData = unreadUserDisputeEvents.getByHash(disputeEventReadRequest.getUserHash());
        unreadUserDisputeEventData.getDisputeEventHashes().remove(disputeEventReadRequest.getDisputeEventHash());
        unreadUserDisputeEvents.put(unreadUserDisputeEventData);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

    public ResponseEntity populateNotReadEvents(DisputePopulateEventsRequest disputePopulateEventsRequest) {

        UnreadUserDisputeEventData unreadUserDisputeEventData = unreadUserDisputeEvents.getByHash(disputePopulateEventsRequest.getUserHash());
        DisputeEventData disputeEventData;
        if(unreadUserDisputeEventData != null) {
            for(Hash disputeEventHash : unreadUserDisputeEventData.getDisputeEventHashes()) {
                disputeEventData = disputeEvents.getByHash(disputeEventHash);
                messagingSender.convertAndSend("/topic/user/" + disputePopulateEventsRequest.getUserHash(), new DisputeEventResponse(disputeEventData, disputePopulateEventsRequest.getUserHash()));
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

    public void notifyOnNewDispute(DisputeData disputeData) {

        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        userHashToEventDisplaySideMap.put(disputeData.getConsumerHash(), ActionSide.Consumer);
        userHashToEventDisplaySideMap.put(disputeData.getMerchantHash(), ActionSide.Merchant);

        updateDisputeHistory(disputeData.getHash(), disputeData, userHashToEventDisplaySideMap, ActionSide.Consumer);
    }

    public void notifyOnDisputeToArbitrators(DisputeData disputeData) {
        if(disputeData.getArbitratorHashes().isEmpty()){
            log.error("Notification on dispute to arbitrators error: Dispute is not assigned to any arbitrator");
            return;
        }
        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        disputeData.getArbitratorHashes().forEach(arbitratorHash -> userHashToEventDisplaySideMap.put(arbitratorHash, ActionSide.Arbitrator));

        updateDisputeHistory(disputeData.getHash(), disputeData, userHashToEventDisplaySideMap, ActionSide.FinancialServer);

    }

    public void notifyOnNewCommentOrDocument(DisputeData disputeData, IDisputeEvent disputeEvent, ActionSide actionSide) {

        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        userHashToEventDisplaySideMap.put(disputeData.getConsumerHash(), ActionSide.Consumer);
        userHashToEventDisplaySideMap.put(disputeData.getMerchantHash(), ActionSide.Merchant);

        updateDisputeHistory(disputeData.getHash(), disputeEvent, userHashToEventDisplaySideMap, actionSide);
    }

    public void notifyOnDisputeStatusChange(DisputeData disputeData) {

        DisputeStatusChangeEventData disputeStatusChangedEventData = new DisputeStatusChangeEventData(disputeData.getHash(), disputeData.getDisputeStatus());

        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        userHashToEventDisplaySideMap.put(disputeData.getConsumerHash(), ActionSide.Consumer);
        userHashToEventDisplaySideMap.put(disputeData.getMerchantHash(), ActionSide.Merchant);
        disputeData.getArbitratorHashes().forEach(arbitratorHash -> userHashToEventDisplaySideMap.put(arbitratorHash, ActionSide.Arbitrator));

        updateDisputeHistory(disputeData.getHash(), disputeStatusChangedEventData, userHashToEventDisplaySideMap, ActionSide.FinancialServer);
    }

    public void notifyOnItemStatusChange(DisputeData disputeData, Long itemId, ActionSide actionSide) {
        DisputeItemStatusChangeEventData disputeIemStatusChangeEventData = new DisputeItemStatusChangeEventData(disputeData.getHash(), itemId, disputeData.getDisputeItem(itemId).getStatus());

        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        userHashToEventDisplaySideMap.put(disputeData.getConsumerHash(), ActionSide.Consumer);
        userHashToEventDisplaySideMap.put(disputeData.getMerchantHash(), ActionSide.Merchant);
        disputeData.getArbitratorHashes().forEach(arbitratorHash -> userHashToEventDisplaySideMap.put(arbitratorHash, ActionSide.Arbitrator));

        updateDisputeHistory(disputeData.getHash(), disputeIemStatusChangeEventData, userHashToEventDisplaySideMap, actionSide);
    }

    public void notifyOnNewItemVote(DisputeItemVoteData disputeItemVoteData) {
        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        userHashToEventDisplaySideMap.put(disputeItemVoteData.getArbitratorHash(), ActionSide.Arbitrator);

        updateDisputeHistory(disputeItemVoteData.getDisputeHash(), disputeItemVoteData, userHashToEventDisplaySideMap, ActionSide.Arbitrator);
    }

    private void updateDisputeHistory(Hash disputeHash, IDisputeEvent eventObject, Map<Hash, ActionSide> userHashToEventDisplaySideMap, ActionSide actionSide) {

        DisputeEventData disputeEventData = new DisputeEventData(eventObject);

        DisputeHistoryData disputeHistoryData = disputeHistory.getByHash(disputeHash);
        if(disputeHistoryData == null) {
            disputeHistoryData = new DisputeHistoryData(disputeHash);
        }
        disputeHistoryData.getDisputeEventHashes().add(disputeEventData.getHash());

        userHashToEventDisplaySideMap.forEach((userHash, eventDisplaySide) -> {
            boolean eventRead = eventDisplaySide.equals(actionSide);
            if(!eventRead) {
                UnreadUserDisputeEventData unreadUserDisputeEventData =  unreadUserDisputeEvents.getByHash(userHash);
                if(unreadUserDisputeEventData == null) {
                    unreadUserDisputeEventData = new UnreadUserDisputeEventData(userHash);
                }
                unreadUserDisputeEventData.getDisputeEventHashes().add(disputeEventData.getHash());
                unreadUserDisputeEvents.put(unreadUserDisputeEventData);
            }
            messagingSender.convertAndSend("/topic/user/" + userHash, new DisputeEventResponse(disputeEventData, userHash, eventDisplaySide, eventRead));
        });

        disputeEvents.put(disputeEventData);
        disputeHistory.put(disputeHistoryData);

    }
}

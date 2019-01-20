package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.crypto.DisputeEventReadCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import io.coti.financialserver.http.DisputeEventReadRequest;
import io.coti.financialserver.http.DisputeEventResponse;
import io.coti.financialserver.model.DisputeEvents;
import io.coti.financialserver.model.DisputeHistory;
import io.coti.financialserver.model.UnreadUserDisputeEvents;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static io.coti.financialserver.http.HttpStringConstants.INVALID_SIGNATURE;
import static io.coti.financialserver.http.HttpStringConstants.SUCCESS;

@Slf4j
@Service
public class WebSocketService {

    @Autowired
    private DisputeEventReadCrypto disputeEventReadCrypto;
    @Autowired
    private UnreadUserDisputeEvents unreadUserDisputeEvents;
    @Autowired
    private DisputeEvents disputeEvents;
    @Autowired
    private DisputeHistory disputeHistory;
    @Autowired
    private SimpMessagingTemplate messagingSender;

    public ResponseEntity<IResponse> eventRead(DisputeEventReadRequest disputeEventReadRequest) {

        if (!disputeEventReadCrypto.verifySignature(disputeEventReadRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        UnreadUserDisputeEventData unreadUserDisputeEventData = unreadUserDisputeEvents.getByHash(disputeEventReadRequest.getUserHash());
        if (unreadUserDisputeEventData != null) {
            ActionSide eventDisplaySide = unreadUserDisputeEventData.getDisputeEventHashToEventDisplaySideMap().get(disputeEventReadRequest.getDisputeEventHash());
            if (eventDisplaySide != null) {
                unreadUserDisputeEventData.getDisputeEventHashToEventDisplaySideMap().remove(disputeEventReadRequest.getDisputeEventHash());
                unreadUserDisputeEvents.put(unreadUserDisputeEventData);
                DisputeEventData disputeEventData = disputeEvents.getByHash(disputeEventReadRequest.getDisputeEventHash());
                notifyOnDisputeEventRead(disputeEventData, disputeEventReadRequest.getUserHash(), eventDisplaySide);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

    public void notifyOnDisputeEventRead(DisputeEventData disputeEventData, Hash userHash, ActionSide eventDisplaySide) {

        messagingSender.convertAndSend("/topic/user/" + userHash, new DisputeEventResponse(disputeEventData, userHash, eventDisplaySide, true));
    }

    public void notifyOnNewDispute(DisputeData disputeData) {

        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        userHashToEventDisplaySideMap.put(disputeData.getConsumerHash(), ActionSide.Consumer);
        userHashToEventDisplaySideMap.put(disputeData.getMerchantHash(), ActionSide.Merchant);

        sendDisputeEvent(disputeData.getHash(), disputeData, userHashToEventDisplaySideMap, ActionSide.Consumer, true);
    }

    public void notifyOnDisputeToArbitrators(DisputeData disputeData) {
        if (disputeData.getArbitratorHashes().isEmpty()) {
            log.error("Notification on dispute to arbitrators error: Dispute is not assigned to any arbitrator");
            return;
        }
        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        disputeData.getArbitratorHashes().forEach(arbitratorHash -> userHashToEventDisplaySideMap.put(arbitratorHash, ActionSide.Arbitrator));

        sendDisputeEvent(disputeData.getHash(), disputeData, userHashToEventDisplaySideMap, ActionSide.FinancialServer, true);

    }

    public void notifyOnNewCommentOrDocument(DisputeData disputeData, IDisputeEvent disputeEvent, ActionSide actionSide) {

        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        userHashToEventDisplaySideMap.put(disputeData.getConsumerHash(), ActionSide.Consumer);
        userHashToEventDisplaySideMap.put(disputeData.getMerchantHash(), ActionSide.Merchant);

        sendDisputeEvent(disputeData.getHash(), disputeEvent, userHashToEventDisplaySideMap, actionSide, false);
    }

    public void notifyOnDisputeStatusChange(DisputeData disputeData, ActionSide actionSide) {

        DisputeStatusChangeEventData disputeStatusChangedEventData = new DisputeStatusChangeEventData(disputeData);

        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        userHashToEventDisplaySideMap.put(disputeData.getConsumerHash(), ActionSide.Consumer);
        userHashToEventDisplaySideMap.put(disputeData.getMerchantHash(), ActionSide.Merchant);
        disputeData.getArbitratorHashes().forEach(arbitratorHash -> userHashToEventDisplaySideMap.put(arbitratorHash, ActionSide.Arbitrator));

        sendDisputeEvent(disputeData.getHash(), disputeStatusChangedEventData, userHashToEventDisplaySideMap, actionSide, true);
    }

    public void notifyOnItemStatusChange(DisputeData disputeData, Long itemId, ActionSide actionSide) {
        DisputeItemStatusChangeEventData disputeIemStatusChangeEventData = new DisputeItemStatusChangeEventData(disputeData, itemId);

        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        userHashToEventDisplaySideMap.put(disputeData.getConsumerHash(), ActionSide.Consumer);
        userHashToEventDisplaySideMap.put(disputeData.getMerchantHash(), ActionSide.Merchant);
        disputeData.getArbitratorHashes().forEach(arbitratorHash -> userHashToEventDisplaySideMap.put(arbitratorHash, ActionSide.Arbitrator));

        sendDisputeEvent(disputeData.getHash(), disputeIemStatusChangeEventData, userHashToEventDisplaySideMap, actionSide, true);
    }

    public void notifyOnNewItemVote(DisputeItemVoteData disputeItemVoteData) {
        Map<Hash, ActionSide> userHashToEventDisplaySideMap = new HashMap<>();
        userHashToEventDisplaySideMap.put(disputeItemVoteData.getArbitratorHash(), ActionSide.Arbitrator);

        sendDisputeEvent(disputeItemVoteData.getDisputeHash(), disputeItemVoteData, userHashToEventDisplaySideMap, ActionSide.Arbitrator, true);
    }

    private void sendDisputeEvent(Hash disputeHash, IDisputeEvent eventObject, Map<Hash, ActionSide> userHashToEventDisplaySideMap, ActionSide actionSide, boolean updateDisputeHistory) {

        DisputeEventData disputeEventData = new DisputeEventData(eventObject);

        userHashToEventDisplaySideMap.forEach((userHash, eventDisplaySide) -> {
            boolean eventRead = eventDisplaySide.equals(actionSide);
            if (!eventRead) {
                UnreadUserDisputeEventData unreadUserDisputeEventData = unreadUserDisputeEvents.getByHash(userHash);
                if (unreadUserDisputeEventData == null) {
                    unreadUserDisputeEventData = new UnreadUserDisputeEventData(userHash);
                }
                unreadUserDisputeEventData.getDisputeEventHashToEventDisplaySideMap().put(disputeEventData.getHash(), eventDisplaySide);
                unreadUserDisputeEvents.put(unreadUserDisputeEventData);
            }

            messagingSender.convertAndSend("/topic/user/" + userHash, new DisputeEventResponse(disputeEventData, userHash, eventDisplaySide, eventRead));
        });
        if (updateDisputeHistory) {
            DisputeHistoryData disputeHistoryData = disputeHistory.getByHash(disputeHash);
            if (disputeHistoryData == null) {
                disputeHistoryData = new DisputeHistoryData(disputeHash);
            }
            disputeHistoryData.getDisputeEventHashToEventDisplayUserMap().putIfAbsent(disputeEventData.getHash(), userHashToEventDisplaySideMap);
            disputeHistory.put(disputeHistoryData);
        }
        disputeEvents.put(disputeEventData);
    }
}

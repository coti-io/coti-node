package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeEventData;
import io.coti.financialserver.data.UnreadUserDisputeEventData;
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
import java.util.List;

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

        UnreadUserDisputeEventData userDisputeEventData = unreadUserDisputeEvents.getByHash(disputeEventReadRequest.getUserHash());
        userDisputeEventData.getDisputeEventHashes().remove(disputeEventReadRequest.getDisputeEventHash());
        unreadUserDisputeEvents.put(userDisputeEventData);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

    public ResponseEntity populateNotReadEvents(DisputePopulateEventsRequest disputePopulateEventsRequest) {

        UnreadUserDisputeEventData userDisputeEventData = unreadUserDisputeEvents.getByHash(disputePopulateEventsRequest.getUserHash());
        DisputeEventData disputeEventData;
        if(userDisputeEventData != null) {
            for(Hash disputeEventHash : userDisputeEventData.getDisputeEventHashes()) {
                disputeEventData = disputeEvents.getByHash(disputeEventHash);
                messagingSender.convertAndSend("/topic/user/" + disputePopulateEventsRequest.getUserHash(), new DisputeEventResponse(disputeEventData, disputePopulateEventsRequest.getUserHash()));
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

    public void notifyOnNewDispute(DisputeData disputeData) {

        List<Hash> usersToUpdate = new ArrayList<>();
        usersToUpdate.add(disputeData.getMerchantHash());

        DisputeEventData disputeEventData = updateDisputeHistory(disputeData.getHash(), disputeData, usersToUpdate);

        messagingSender.convertAndSend("/topic/user/" + disputeData.getConsumerHash(), new DisputeEventResponse(disputeEventData, disputeData.getConsumerHash()));
    }

    public void notifyOnNewCommentOrDocument(DisputeData disputeData, IDisputeEvent disputeEvent, ActionSide actionSide) {

        List<Hash> usersToUpdate = new ArrayList<>();

        if(actionSide.equals(ActionSide.Consumer)) {
            usersToUpdate.add(disputeData.getMerchantHash());
            DisputeEventData disputeEventData = updateDisputeHistory(disputeData.getHash(), disputeEvent, usersToUpdate);
            messagingSender.convertAndSend("/topic/user/" + disputeData.getConsumerHash(), new DisputeEventResponse(disputeEventData, disputeData.getConsumerHash()));
        }
        else {
            usersToUpdate.add(disputeData.getConsumerHash());
            DisputeEventData disputeEventData = updateDisputeHistory(disputeData.getHash(), disputeEvent, usersToUpdate);
            messagingSender.convertAndSend("/topic/user/" + disputeData.getMerchantHash(), new DisputeEventResponse(disputeEventData, disputeData.getMerchantHash()));
        }
    }

    public void notifyOnDisputeStatusChange(DisputeData disputeData) {

        DisputeStatusChangedEvent disputeStatusChangedEvent = new DisputeStatusChangedEvent(disputeData.getHash(), disputeData.getDisputeStatus());

        List<Hash> usersToUpdate = new ArrayList<>();
        usersToUpdate.add(disputeData.getConsumerHash());
        usersToUpdate.add(disputeData.getMerchantHash());

        if(!disputeData.getArbitratorHashes().isEmpty()) {
            usersToUpdate.addAll(disputeData.getArbitratorHashes());
        }

        updateDisputeHistory(disputeData.getHash(), disputeStatusChangedEvent, usersToUpdate);
    }

    public void notifyOnItemStatusChange(DisputeData disputeData, Long itemId) {
        ItemStatusChangedEvent disputeStatusChangeEvent = new ItemStatusChangedEvent(disputeData.getHash(), itemId, disputeData.getDisputeItem(itemId).getStatus());

        List<Hash> usersToUpdate = new ArrayList<>();
        usersToUpdate.add(disputeData.getConsumerHash());
        usersToUpdate.add(disputeData.getMerchantHash());

        if(!disputeData.getArbitratorHashes().isEmpty()) {
            usersToUpdate.addAll(disputeData.getArbitratorHashes());
        }

        updateDisputeHistory(disputeData.getHash(), disputeStatusChangeEvent, usersToUpdate);
    }

    private DisputeEventData updateDisputeHistory(Hash disputeHash, IDisputeEvent eventObject, List<Hash> usersToUpdate) {

        DisputeEventData disputeEventData = new DisputeEventData(eventObject);

        DisputeHistoryData disputeHistoryData = disputeHistory.getByHash(disputeHash);
        if(disputeHistoryData == null) {
            disputeHistoryData = new DisputeHistoryData(disputeHash);
        }
        disputeHistoryData.getDisputeEventHashes().add(disputeEventData.getHash());

        UserDisputeEventData userDisputeEventData;
        for(Hash userToUpdate : usersToUpdate) {
            userDisputeEventData = unreadUserDisputeEvents.getByHash(userToUpdate);
            if(userDisputeEventData == null) {
                userDisputeEventData = new UserDisputeEventData(userToUpdate);
            }
            userDisputeEventData.getDisputeEventHashes().add(disputeEventData.getHash());
            unreadUserDisputeEvents.put(userDisputeEventData);
            messagingSender.convertAndSend("/topic/user/" + userToUpdate, new DisputeEventResponse(disputeEventData, userToUpdate));
        }

        disputeEvents.put(disputeEventData);
        disputeHistory.put(disputeHistoryData);

        return disputeEventData;
    }
}

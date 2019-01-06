package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.financialserver.data.*;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import io.coti.financialserver.http.DisputeEventReadRequest;
import io.coti.financialserver.http.DisputePopulateEventsRequest;
import io.coti.financialserver.model.DisputeEvents;
import io.coti.financialserver.model.DisputeHistory;
import io.coti.financialserver.model.UserDisputeEvents;
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
    private UserDisputeEvents userDisputeEvents;
    @Autowired
    private DisputeEvents disputeEvents;
    @Autowired
    private DisputeHistory disputeHistory;
    @Autowired
    private SimpMessagingTemplate messagingSender;

    public ResponseEntity eventRead(DisputeEventReadRequest disputeEventReadRequest) {

        UserDisputeEventData userDisputeEventData = userDisputeEvents.getByHash(disputeEventReadRequest.getUserHash());
        userDisputeEventData.getDisputeEventHashes().remove(disputeEventReadRequest.getDisputeEventHash());

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

    public ResponseEntity populateNotReadEvents(DisputePopulateEventsRequest disputePopulateEventsRequest) {

        UserDisputeEventData userDisputeEventData = userDisputeEvents.getByHash(disputePopulateEventsRequest.getUserHash());
        DisputeEventData disputeEventData;
        if(userDisputeEventData.getDisputeEventHashes() != null) {
            for(Hash disputeEventHash : userDisputeEventData.getDisputeEventHashes()) {
                disputeEventData = disputeEvents.getByHash(disputeEventHash);
                messagingSender.convertAndSend("/topic/user/" + disputePopulateEventsRequest.getUserHash(), disputeEventData);
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(new Response(SUCCESS, STATUS_SUCCESS));
    }

    public void notifyOnNewDispute(DisputeData disputeData) {

        List<Hash> usersToUpdate = new ArrayList<>();
        usersToUpdate.add(disputeData.getMerchantHash());

        updateDisputeHistory(disputeData.getHash(), disputeData, usersToUpdate);

        messagingSender.convertAndSend("/topic/user/" + disputeData.getConsumerHash(), disputeData);
    }

    public void notifyOnNewCommentOrDocument(DisputeData disputeData, IDisputeEvent disputeEvent, ActionSide actionSide) {

        List<Hash> usersToUpdate = new ArrayList<>();
        if(actionSide.equals(ActionSide.Consumer)) {
            usersToUpdate.add(disputeData.getMerchantHash());
            messagingSender.convertAndSend("/topic/user/" + disputeData.getConsumerHash(), disputeData);
        }
        else {
            usersToUpdate.add(disputeData.getConsumerHash());
            messagingSender.convertAndSend("/topic/user/" + disputeData.getMerchantHash(), disputeData);
        }

        updateDisputeHistory(disputeData.getHash(), disputeEvent, usersToUpdate);
    }

    public void notifyOnDisputeStatusChange(DisputeData disputeData) {

        DisputeStatusChangeEvent disputeStatusChangeEvent = new DisputeStatusChangeEvent(disputeData.getHash(), disputeData.getDisputeStatus());

        List<Hash> usersToUpdate = new ArrayList<>();
        usersToUpdate.add(disputeData.getConsumerHash());
        usersToUpdate.add(disputeData.getMerchantHash());

        if(!disputeData.getArbitratorHashes().isEmpty()) {
            usersToUpdate.addAll(disputeData.getArbitratorHashes());
        }

        updateDisputeHistory(disputeData.getHash(), disputeStatusChangeEvent, usersToUpdate);
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

    private void updateDisputeHistory(Hash disputeHash, IDisputeEvent eventObject, List<Hash> usersToUpdate) {

        DisputeEventData disputeEventData = new DisputeEventData(eventObject);

        DisputeHistoryData disputeHistoryData = disputeHistory.getByHash(disputeHash);
        if(disputeHistoryData == null) {
            disputeHistoryData = new DisputeHistoryData(disputeHash);
        }
        disputeHistoryData.getDisputeEventHashes().add(disputeEventData.getHash());

        UserDisputeEventData userDisputeEventData;
        for(Hash userToUpdate : usersToUpdate) {
            userDisputeEventData = userDisputeEvents.getByHash(userToUpdate);
            if(userDisputeEventData == null) {
                userDisputeEventData = new UserDisputeEventData(userToUpdate);
            }
            userDisputeEventData.getDisputeEventHashes().add(disputeEventData.getHash());
            userDisputeEvents.put(userDisputeEventData);
            messagingSender.convertAndSend("/topic/user/" + userToUpdate, disputeEventData);
        }

        disputeEvents.put(disputeEventData);
        disputeHistory.put(disputeHistoryData);
    }
}

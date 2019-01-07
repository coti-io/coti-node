package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.data.*;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import io.coti.financialserver.http.data.GetCommentResponseData;
import io.coti.financialserver.http.data.GetDisputeResponseClass;
import lombok.Data;

import java.time.Instant;

@Data
public class DisputeEventDataSent {

    private String hash;
    private Instant creationTime;
    private IResponse eventObject;

    public DisputeEventDataSent(DisputeEventData disputeEventData, Hash userHash) {

        IDisputeEvent disputeEvent = disputeEventData.getEventObject();
        if(disputeEvent instanceof DisputeData) {
            DisputeData disputeData = ((DisputeData) disputeEventData.getEventObject());
            eventObject = GetDisputeResponseClass.valueOf(getActionSide(disputeData, userHash).toString()).getNewInstance(disputeData, userHash);
        }
        else if(disputeEvent instanceof DisputeCommentData) {
            DisputeCommentData disputeCommentData = ((DisputeCommentData) disputeEventData.getEventObject());
            eventObject = new GetCommentResponseData(disputeCommentData);
        }
        else if(disputeEvent instanceof DisputeDocumentData) {
            DisputeDocumentData disputeDocumentData = ((DisputeDocumentData) disputeEventData.getEventObject());
            eventObject = new NewDocumentResponse(disputeDocumentData);
        }
        else if(disputeEvent instanceof DisputeStatusChangedEvent) {
            DisputeStatusChangedEvent disputeStatusChangedEvent = ((DisputeStatusChangedEvent) disputeEventData.getEventObject());
            eventObject = new DisputeStatusChangedResponse(disputeStatusChangedEvent);
        }
        else if(disputeEvent instanceof ItemStatusChangedEvent) {
            ItemStatusChangedEvent itemStatusChangedEvent = ((ItemStatusChangedEvent) disputeEventData.getEventObject());
            eventObject = new ItemStatusChangedResponse(itemStatusChangedEvent);
        }

        hash = disputeEventData.getHash().toHexString();
        creationTime = disputeEventData.getCreationTime();
    }

    private ActionSide getActionSide(DisputeData disputeData, Hash actionInitiatorHash) {

        if (disputeData.getConsumerHash().equals(actionInitiatorHash)) {
            return ActionSide.Consumer;
        } else {
            return ActionSide.Merchant;
        }
    }
}

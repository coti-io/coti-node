package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.data.DisputeCommentData;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeDocumentData;
import io.coti.financialserver.data.DisputeEventData;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import io.coti.financialserver.http.data.GetCommentResponseData;
import io.coti.financialserver.http.data.GetDisputeResponseClass;
import lombok.Data;

import java.time.Instant;

@Data
public class DisputeEventResponse {

    private String hash;
    private Instant creationTime;
    private IResponse eventObject;

    public DisputeEventResponse(DisputeEventData disputeEventData, Hash userHash) {

        IDisputeEvent disputeEvent = disputeEventData.getEventObject();
        if(disputeEvent instanceof DisputeData) {
            DisputeData disputeData = ((DisputeData) disputeEventData.getEventObject());
            disputeData.setActionSideAndMessageReceiverHash(userHash);
            eventObject = GetDisputeResponseClass.valueOf(disputeData.getActionSide().toString()).getNewInstance(disputeData, userHash);
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
}

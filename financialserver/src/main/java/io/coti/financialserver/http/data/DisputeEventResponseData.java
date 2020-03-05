package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.data.interfaces.IResponseData;
import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.data.DisputeEventData;
import io.coti.financialserver.data.FinancialServerEvent;
import io.coti.financialserver.http.data.interfaces.IDisputeEventResponseData;
import lombok.Data;

import java.time.Instant;

@Data
public class DisputeEventResponseData implements IResponseData {

    private String hash;
    private Instant creationTime;
    private ActionSide eventDisplaySide;
    private FinancialServerEvent event;
    private IDisputeEventResponseData eventObject;
    private boolean eventRead;

    public DisputeEventResponseData(DisputeEventData disputeEventData, Hash userHash, ActionSide eventDisplaySide, boolean eventRead) {

        hash = disputeEventData.getHash().toString();
        creationTime = disputeEventData.getCreationTime();
        this.eventDisplaySide = eventDisplaySide;
        DisputeEventResponseDataClass disputeEventResponseDataClass = DisputeEventResponseDataClass.getByDisputeEventClass(disputeEventData.getEventObject().getClass());
        event = disputeEventResponseDataClass.getFinancialServerEvent();
        eventObject = disputeEventResponseDataClass.getEventObject(disputeEventData, userHash, eventDisplaySide);
        this.eventRead = eventRead;

    }
}

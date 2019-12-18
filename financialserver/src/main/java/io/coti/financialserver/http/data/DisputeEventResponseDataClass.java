package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.*;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import io.coti.financialserver.http.data.interfaces.IDisputeEventResponseData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public enum DisputeEventResponseDataClass {
    DISPUTE_DATA(DisputeData.class, GetDisputeResponseData.class, FinancialServerEvent.NEW_DISPUTE) {
        @Override
        public IDisputeEventResponseData getEventObject(DisputeEventData disputeEventData, Hash userHash, ActionSide evenDisplaySide) {
            return GetDisputeResponseClass.valueOf(evenDisplaySide.toString()).getNewInstance((DisputeData) disputeEventData.getEventObject(), userHash);
        }
    },
    DISPUTE_COMMENT_DATA(DisputeCommentData.class, GetCommentResponseData.class, FinancialServerEvent.NEW_DISPUTE_COMMENT),
    DISPUTE_DOCUMENT_DATA(DisputeDocumentData.class, DocumentNameResponseData.class, FinancialServerEvent.NEW_DISPUTE_DOCUMENT),
    DISPUTE_STATUS_CHANGE_EVENT_DATA(DisputeStatusChangeEventData.class, DisputeStatusChangeResponseData.class, FinancialServerEvent.DISPUTE_STATUS_UPDATED),
    DISPUTE_ITEM_STATUS_CHANGE_EVENT_DATA(DisputeItemStatusChangeEventData.class, DisputeItemStatusChangeResponseData.class, FinancialServerEvent.DISPUTE_ITEM_STATUS_UPDATED),
    DISPUTE_ITEM_VOTE_DATA(DisputeItemVoteData.class, DisputeItemVoteResponseData.class, FinancialServerEvent.NEW_DISPUTE_ITEM_VOTE);


    private Class<? extends IDisputeEvent> disputeEventClass;
    private Class<? extends IDisputeEventResponseData> disputeEventResponseClass;
    private FinancialServerEvent financialServerEvent;

    <T extends IDisputeEvent, S extends IDisputeEventResponseData> DisputeEventResponseDataClass(Class<T> disputeEventClass, Class<S> disputeEventResponseClass, FinancialServerEvent financialServerEvent) {
        this.disputeEventClass = disputeEventClass;
        this.disputeEventResponseClass = disputeEventResponseClass;
        this.financialServerEvent = financialServerEvent;
    }

    public IDisputeEventResponseData getEventObject(DisputeEventData disputeEventData, Hash userHash, ActionSide evenDisplaySide) {
        try {
            Constructor<? extends IDisputeEventResponseData> constructor = disputeEventResponseClass.getConstructor(disputeEventClass);
            return constructor.newInstance(disputeEventClass.cast(disputeEventData.getEventObject()));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public FinancialServerEvent getFinancialServerEvent() {
        return financialServerEvent;
    }
}

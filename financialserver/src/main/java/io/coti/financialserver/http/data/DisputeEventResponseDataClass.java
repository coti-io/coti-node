package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.*;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import io.coti.financialserver.http.data.interfaces.IDisputeEventResponseData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public enum DisputeEventResponseDataClass {
    DisputeData(DisputeData.class, GetDisputeResponseData.class, FinancialServerEvent.NewDispute) {
        @Override
        public IDisputeEventResponseData getEventObject(DisputeEventData disputeEventData, Hash userHash, ActionSide evenDisplaySide) {
            return GetDisputeResponseClass.valueOf(evenDisplaySide.toString()).getNewInstance((DisputeData) disputeEventData.getEventObject(), userHash);
        }
    },
    DisputeCommentData(DisputeCommentData.class, GetCommentResponseData.class, FinancialServerEvent.NewDisputeComment),
    DisputeDocumentData(DisputeDocumentData.class, DocumentNameResponseData.class, FinancialServerEvent.NewDisputeDocument),
    DisputeStatusChangeEventData(DisputeStatusChangeEventData.class, DisputeStatusChangeResponseData.class, FinancialServerEvent.DisputeStatusUpdated),
    DisputeItemStatusChangeEventData(DisputeItemStatusChangeEventData.class, DisputeItemStatusChangeResponseData.class, FinancialServerEvent.DisputeItemStatusUpdated),
    DisputeItemVoteData(DisputeItemVoteData.class, DisputeItemVoteResponseData.class, FinancialServerEvent.NewDisputeItemVote);


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

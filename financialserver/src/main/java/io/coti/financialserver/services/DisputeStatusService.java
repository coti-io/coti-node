package io.coti.financialserver.services;

import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeStatus;
import io.coti.financialserver.exceptions.DisputeChangeStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

import static io.coti.financialserver.http.HttpStringConstants.DISPUTE_STATUS_FINAL;
import static io.coti.financialserver.http.HttpStringConstants.DISPUTE_STATUS_INVALID_CHANGE;


public enum DisputeStatusService {
    CANCELED_BY_CONSUMER(DisputeStatus.CANCELED_BY_CONSUMER, EnumSet.of(DisputeStatus.RECALL), ActionSide.CONSUMER, true),
    CLAIM(DisputeStatus.CLAIM, EnumSet.of(DisputeStatus.RECALL), ActionSide.FINANCIAL_SERVER, false),
    CLOSED(DisputeStatus.CLOSED, EnumSet.of(DisputeStatus.RECALL, DisputeStatus.CLAIM), ActionSide.FINANCIAL_SERVER, true);

    private DisputeStatus newDisputeStatus;
    private Set<DisputeStatus> previousDisputeStatuses;
    private ActionSide actionSide;
    private boolean finalStatus;
    protected WebSocketService webSocketService;

    DisputeStatusService(DisputeStatus newDisputeStatus, Set<DisputeStatus> previousDisputeStatuses, ActionSide actionSide, boolean finalStatus) {
        this.newDisputeStatus = newDisputeStatus;
        this.previousDisputeStatuses = previousDisputeStatuses;
        this.actionSide = actionSide;
        this.finalStatus = finalStatus;
    }

    public boolean isFinalStatus() {
        return finalStatus;
    }

    public void changeStatus(DisputeData disputeData) throws DisputeChangeStatusException {
        if (!disputeData.getDisputeStatus().equals(DisputeStatus.RECALL) && valueOf(disputeData.getDisputeStatus().toString()).isFinalStatus()) {
            throw new DisputeChangeStatusException(DISPUTE_STATUS_FINAL);
        }
        if (!previousDisputeStatuses.contains(disputeData.getDisputeStatus())) {
            throw new DisputeChangeStatusException(DISPUTE_STATUS_INVALID_CHANGE);
        }

        disputeData.setDisputeStatus(newDisputeStatus);
        if (isFinalStatus()) {
            disputeData.setClosedTime(Instant.now());
        }
        webSocketService.notifyOnDisputeStatusChange(disputeData, actionSide);
    }

    @Component
    public static class DisputeStatusServiceInjector {
        @Autowired
        private WebSocketService webSocketService;

        @PostConstruct
        public void postConstruct() {
            for (DisputeStatusService disputeStatusService : EnumSet.allOf(DisputeStatusService.class)) {
                disputeStatusService.webSocketService = webSocketService;
            }
        }
    }

}

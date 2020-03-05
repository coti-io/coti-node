package io.coti.financialserver.services;

import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeStatus;
import io.coti.financialserver.exceptions.DisputeChangeStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static io.coti.financialserver.http.HttpStringConstants.DISPUTE_STATUS_FINAL;
import static io.coti.financialserver.http.HttpStringConstants.DISPUTE_STATUS_INVALID_CHANGE;


public enum DisputeStatusService {
    CANCELED_BY_CONSUMER(DisputeStatus.CanceledByConsumer, EnumSet.of(DisputeStatus.Recall), ActionSide.Consumer, true),
    CLAIM(DisputeStatus.Claim, EnumSet.of(DisputeStatus.Recall), ActionSide.FinancialServer, false),
    CLOSED(DisputeStatus.Closed, EnumSet.of(DisputeStatus.Recall, DisputeStatus.Claim), ActionSide.FinancialServer, true);

    private DisputeStatus newDisputeStatus;
    private Set<DisputeStatus> previousDisputeStatuses;
    private ActionSide actionSide;
    private boolean finalStatus;
    protected WebSocketService webSocketService;

    private static class DisputeStatusServices {
        private static final Map<DisputeStatus, DisputeStatusService> disputeStatusServiceMap = new EnumMap<>(DisputeStatus.class);
    }

    DisputeStatusService(DisputeStatus newDisputeStatus, Set<DisputeStatus> previousDisputeStatuses, ActionSide actionSide, boolean finalStatus) {
        this.newDisputeStatus = newDisputeStatus;
        DisputeStatusServices.disputeStatusServiceMap.put(newDisputeStatus, this);
        this.previousDisputeStatuses = previousDisputeStatuses;
        this.actionSide = actionSide;
        this.finalStatus = finalStatus;
    }

    public static DisputeStatusService getByDisputeStatus(DisputeStatus disputeStatus) {
        return DisputeStatusServices.disputeStatusServiceMap.get(disputeStatus);
    }

    public boolean isFinalStatus() {
        return finalStatus;
    }

    public void changeStatus(DisputeData disputeData) {
        if (!disputeData.getDisputeStatus().equals(DisputeStatus.Recall) && valueOf(disputeData.getDisputeStatus().toString()).isFinalStatus()) {
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

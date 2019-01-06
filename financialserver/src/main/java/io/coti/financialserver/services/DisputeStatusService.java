package io.coti.financialserver.services;

import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeStatus;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

import static io.coti.financialserver.http.HttpStringConstants.DISPUTE_STATUS_FINAL;
import static io.coti.financialserver.http.HttpStringConstants.DISPUTE_STATUS_INVALID_CHANGE;


public enum DisputeStatusService {
    CanceledByConsumer(DisputeStatus.CanceledByConsumer, EnumSet.of(DisputeStatus.Recall), true),
    Claim(DisputeStatus.Claim, EnumSet.of(DisputeStatus.Recall), false),
    Closed(DisputeStatus.Closed, EnumSet.of(DisputeStatus.Recall, DisputeStatus.Claim), true);

    private DisputeStatus newDisputeStatus;
    private Set<DisputeStatus> previousDisputeStatuses;
    private boolean finalStatus;

    DisputeStatusService(DisputeStatus newDisputeStatus, Set<DisputeStatus> previousDisputeStatuses, boolean finalStatus) {
        this.newDisputeStatus = newDisputeStatus;
        this.previousDisputeStatuses = previousDisputeStatuses;
        this.finalStatus = finalStatus;
    }

    public boolean isFinalStatus() {
        return finalStatus;
    }

    public void changeStatus(DisputeData disputeData) throws Exception {
        if (!disputeData.getDisputeStatus().equals(DisputeStatus.Recall) && valueOf(disputeData.getDisputeStatus().toString()).isFinalStatus()) {
            throw new Exception(DISPUTE_STATUS_FINAL);
        }
        if (!previousDisputeStatuses.contains(disputeData.getDisputeStatus())) {
            throw new Exception(DISPUTE_STATUS_INVALID_CHANGE);
        }
        disputeData.setDisputeStatus(newDisputeStatus);
        if(isFinalStatus()){
            disputeData.setClosedTime(Instant.now());
        }

    }

}

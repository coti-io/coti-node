package io.coti.financialserver.services;

import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.DisputeItemStatus;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.coti.financialserver.http.HttpStringConstants.DISPUTE_ITEM_NOT_FOUND;
import static io.coti.financialserver.http.HttpStringConstants.DISPUTE_ITEM_STATUS_FINAL;
import static io.coti.financialserver.http.HttpStringConstants.DISPUTE_ITEM_STATUS_INVALID_CHANGE;

public enum DisputeItemStatusService {
    AcceptedByMerchant(DisputeItemStatus.AcceptedByMerchant, EnumSet.of(DisputeItemStatus.Recall), true),
    RejectedByMerchant(DisputeItemStatus.RejectedByMerchant, EnumSet.of(DisputeItemStatus.Recall), false),
    CanceledByConsumer(DisputeItemStatus.CanceledByConsumer, EnumSet.of(DisputeItemStatus.Recall), true),
    Claim(DisputeItemStatus.Claim, EnumSet.of(DisputeItemStatus.Recall, DisputeItemStatus.RejectedByMerchant), false),
    AcceptedByArbitrators(DisputeItemStatus.AcceptedByArbitrators, EnumSet.of(DisputeItemStatus.Claim), true),
    RejectedByArbitrators(DisputeItemStatus.RejectedByArbitrators, EnumSet.of(DisputeItemStatus.Claim), true);

    private DisputeItemStatus newDisputeStatus;
    private Set<DisputeItemStatus> previousDisputeStatuses;
    private boolean finalStatus;

    DisputeItemStatusService(DisputeItemStatus newDisputeStatus, Set<DisputeItemStatus> previousDisputeStatuses, boolean finalStatus) {
        this.newDisputeStatus = newDisputeStatus;
        this.previousDisputeStatuses = previousDisputeStatuses;
        this.finalStatus = finalStatus;
    }

    public boolean isFinalStatus() {
        return finalStatus;
    }

    public void changeStatus(DisputeData disputeData, Long itemId) throws Exception {
        Stream<DisputeItemData> disputeItemDataStream = disputeData.getDisputeItems().stream().filter(disputeItemData -> disputeItemData.getId().equals(itemId));
        if(disputeItemDataStream.count() == 0){
            throw new Exception(DISPUTE_ITEM_NOT_FOUND);
        }
        DisputeItemData disputeItemData = disputeItemDataStream.findFirst().get();
        if(valueOf(disputeItemData.getStatus().toString()).isFinalStatus()){
            throw new Exception(DISPUTE_ITEM_STATUS_FINAL);
        }
        if(!previousDisputeStatuses.contains(disputeItemData.getStatus())){
            throw new Exception(DISPUTE_ITEM_STATUS_INVALID_CHANGE);
        }
        disputeItemData.setStatus(newDisputeStatus);
    }

    public boolean isFinalStatusForAllItems(DisputeData disputeData) {
        List<DisputeItemData> disputeItems = disputeData.getDisputeItems();
        for(DisputeItemData disputeItemData : disputeItems){
            if(!valueOf(disputeItemData.getStatus().toString()).isFinalStatus()){
                return false;
            }
        }
        return true;
    }
}

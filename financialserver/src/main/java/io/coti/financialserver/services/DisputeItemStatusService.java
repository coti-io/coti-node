package io.coti.financialserver.services;

import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.DisputeItemStatus;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.coti.financialserver.http.HttpStringConstants.*;

public enum DisputeItemStatusService {
    AcceptedByMerchant(DisputeItemStatus.AcceptedByMerchant, EnumSet.of(DisputeItemStatus.Recall), true, true) {
        public void changeOtherDisputeItemsStatuses(DisputeData disputeData, Long itemId) {
            this.RejectedByMerchant.changeOtherDisputeItemsStatuses(disputeData, itemId);
        }

        public void changeDisputeStatus(DisputeData disputeData) throws Exception {
            Stream<DisputeItemData> disputeItemDataStream = disputeData.getDisputeItems().stream().filter(disputeItemData -> disputeItemData.getStatus().equals(DisputeItemStatus.RejectedByMerchant));
            if (disputeItemDataStream.count() == 0) {
                DisputeStatusService.Closed.changeStatus(disputeData);
            } else {
                this.Claim.changeDisputeStatus(disputeData);
            }

        }
    },
    RejectedByMerchant(DisputeItemStatus.RejectedByMerchant, EnumSet.of(DisputeItemStatus.Recall), false, false) {
        public void changeOtherDisputeItemsStatuses(DisputeData disputeData, Long itemId) {
            Stream<DisputeItemData> disputeItemDataStream = disputeData.getDisputeItems().stream().filter(disputeItemData -> !disputeItemData.getId().equals(itemId));
            if (disputeItemDataStream.count() == 0 || existRecallDisputeItems(disputeData)) {
                return;
            }
            List<DisputeItemData> disputeItemDataList = disputeItemDataStream.collect(Collectors.toList());
            for (DisputeItemData disputeItemData : disputeItemDataList) {
                if (disputeItemData.getStatus().equals(DisputeItemStatus.RejectedByMerchant)) {
                    disputeItemData.setStatus(DisputeItemStatus.Claim);
                }
            }
        }

        public void changeDisputeStatus(DisputeData disputeData) throws Exception {
            this.Claim.changeDisputeStatus(disputeData);
        }
    },
    CanceledByConsumer(DisputeItemStatus.CanceledByConsumer, EnumSet.of(DisputeItemStatus.Recall), true, false) {
        public void changeOtherDisputeItemsStatuses(DisputeData disputeData, Long itemId) {
            this.RejectedByMerchant.changeOtherDisputeItemsStatuses(disputeData, itemId);
        }

        public void changeDisputeStatus(DisputeData disputeData) throws Exception {
            Stream<DisputeItemData> disputeItemDataStream = disputeData.getDisputeItems().stream().filter(disputeItemData -> disputeItemData.getStatus().equals(DisputeItemStatus.RejectedByMerchant));
            if (disputeItemDataStream.count() == 0) {
                DisputeStatusService.Closed.changeStatus(disputeData);
            } else {
                this.Claim.changeDisputeStatus(disputeData);
            }
        }
    },
    Claim(DisputeItemStatus.Claim, EnumSet.of(DisputeItemStatus.Recall, DisputeItemStatus.RejectedByMerchant), false, false) {
        public void changeDisputeStatus(DisputeData disputeData) throws Exception {
            for (DisputeItemData disputeItemData : disputeData.getDisputeItems()) {
                if (disputeItemData.getStatus().equals(DisputeItemStatus.Recall)) {
                    return;
                }
            }
            DisputeStatusService.Claim.changeStatus(disputeData);
        }
    },
    AcceptedByArbitrators(DisputeItemStatus.AcceptedByArbitrators, EnumSet.of(DisputeItemStatus.Claim), true, true) {
        public void changeDisputeStatus(DisputeData disputeData) throws Exception {
            for (DisputeItemData disputeItemData : disputeData.getDisputeItems()) {
                if (disputeItemData.getStatus().equals(DisputeItemStatus.Claim)) {
                    return;
                }
            }
            DisputeStatusService.Closed.changeStatus(disputeData);
        }
    },
    RejectedByArbitrators(DisputeItemStatus.RejectedByArbitrators, EnumSet.of(DisputeItemStatus.Claim), true, false) {
        public void changeDisputeStatus(DisputeData disputeData) throws Exception {
            this.AcceptedByArbitrators.changeDisputeStatus(disputeData);
        }
    };

    private DisputeItemStatus newDisputeItemStatus;
    private Set<DisputeItemStatus> previousDisputeItemStatuses;
    private boolean finalStatus;
    private boolean refundable;

    DisputeItemStatusService(DisputeItemStatus newDisputeItemStatus, Set<DisputeItemStatus> previousDisputeItemStatuses, boolean finalStatus, boolean refundable) {
        this.newDisputeItemStatus = newDisputeItemStatus;
        this.previousDisputeItemStatuses = previousDisputeItemStatuses;
        this.finalStatus = finalStatus;
        this.refundable = refundable;
    }

    public boolean isFinalStatus() {
        return finalStatus;
    }

    public boolean isRefundable() {
        return refundable;
    }

    public void changeStatus(DisputeData disputeData, Long itemId) throws Exception {
        Stream<DisputeItemData> disputeItemDataStream = disputeData.getDisputeItems().stream().filter(disputeItemData -> disputeItemData.getId().equals(itemId));
        if (disputeItemDataStream.count() == 0) {
            throw new Exception(DISPUTE_ITEM_NOT_FOUND);
        }
        DisputeItemData disputeItemData = disputeItemDataStream.findFirst().get();
        if (valueOf(disputeItemData.getStatus().toString()).isFinalStatus()) {
            throw new Exception(DISPUTE_ITEM_STATUS_FINAL);
        }
        if (!previousDisputeItemStatuses.contains(disputeItemData.getStatus())) {
            throw new Exception(DISPUTE_ITEM_STATUS_INVALID_CHANGE);
        }
        disputeItemData.setStatus(newDisputeItemStatus);
        changeOtherDisputeItemsStatuses(disputeData, itemId);
        changeDisputeStatus(disputeData);
    }

    public boolean existRecallDisputeItems(DisputeData disputeData) {
        Stream<DisputeItemData> disputeItemDataStream = disputeData.getDisputeItems().stream().filter(disputeItemData -> disputeItemData.getStatus().equals(DisputeItemStatus.Recall));
        return disputeItemDataStream.count() != 0;
    }

    public void changeOtherDisputeItemsStatuses(DisputeData disputeData, Long itemId) {

    }

    abstract void changeDisputeStatus(DisputeData disputeData) throws Exception;


    public boolean isFinalStatusForAllItems(DisputeData disputeData) {
        List<DisputeItemData> disputeItems = disputeData.getDisputeItems();
        for (DisputeItemData disputeItemData : disputeItems) {
            if (!valueOf(disputeItemData.getStatus().toString()).isFinalStatus()) {
                return false;
            }
        }
        return true;
    }
}

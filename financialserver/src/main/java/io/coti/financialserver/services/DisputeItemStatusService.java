package io.coti.financialserver.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.DisputeItemStatus;
import io.coti.financialserver.exceptions.DisputeItemChangeStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.coti.financialserver.http.HttpStringConstants.*;

public enum DisputeItemStatusService {

    ACCEPTED_BY_MERCHANT(DisputeItemStatus.ACCEPTED_BY_MERCHANT, EnumSet.of(DisputeItemStatus.RECALL), ActionSide.MERCHANT, true, true, true) {
        @Override
        public void changeDisputeItemsStatuses(DisputeData disputeData) {
            changePreClaimDisputeItemsStatuses(disputeData);
        }

        @Override
        public void changeDisputeStatus(DisputeData disputeData) {
            if (!existClaimDisputeItems(disputeData)) {
                DisputeStatusService.CLOSED.changeStatus(disputeData);
            } else if (!existRecallDisputeItems(disputeData)) {
                DisputeStatusService.CLAIM.changeStatus(disputeData);
            }

        }
    },
    REJECTED_BY_MERCHANT(DisputeItemStatus.REJECTED_BY_MERCHANT, EnumSet.of(DisputeItemStatus.RECALL), ActionSide.MERCHANT, false, false, true) {
        @Override
        public void changeDisputeItemsStatuses(DisputeData disputeData) {
            changePreClaimDisputeItemsStatuses(disputeData);
        }

        @Override
        public void changeDisputeStatus(DisputeData disputeData) {
            DisputeItemStatusService.CLAIM.changeDisputeStatus(disputeData);
        }
    },
    CANCELED_BY_CONSUMER(DisputeItemStatus.CANCELED_BY_CONSUMER, EnumSet.of(DisputeItemStatus.RECALL), ActionSide.CONSUMER, true, false, true) {
        @Override
        public void changeDisputeItemsStatuses(DisputeData disputeData) {
            changePreClaimDisputeItemsStatuses(disputeData);
        }

        @Override
        public void changeDisputeStatus(DisputeData disputeData) {
            if (isCanceledByConsumerForAllItems(disputeData)) {
                DisputeStatusService.CANCELED_BY_CONSUMER.changeStatus(disputeData);
            } else {
                DisputeItemStatusService.ACCEPTED_BY_MERCHANT.changeDisputeStatus(disputeData);
            }
        }

        private boolean isCanceledByConsumerForAllItems(DisputeData disputeData) {
            List<DisputeItemData> disputeItems = disputeData.getDisputeItems();
            return disputeItems.stream().filter(disputeItemData -> disputeItemData.getStatus().equals(DisputeItemStatus.CANCELED_BY_CONSUMER)).count() == disputeItems.size();
        }
    },
    CLAIM(DisputeItemStatus.CLAIM, EnumSet.of(DisputeItemStatus.RECALL, DisputeItemStatus.REJECTED_BY_MERCHANT), ActionSide.FINANCIAL_SERVER, false, false, false) {
        @Override
        public void changeDisputeStatus(DisputeData disputeData) {
            if (existRecallDisputeItems(disputeData)) {
                return;
            }
            DisputeStatusService.CLAIM.changeStatus(disputeData);
        }
    },
    ACCEPTED_BY_ARBITRATORS(DisputeItemStatus.ACCEPTED_BY_ARBITRATORS, EnumSet.of(DisputeItemStatus.CLAIM), ActionSide.FINANCIAL_SERVER, true, true, false) {
        @Override
        public void changeDisputeStatus(DisputeData disputeData) {
            changeDisputeStatusForPostClaimDisputeItem(disputeData);
        }
    },
    REJECTED_BY_ARBITRATORS(DisputeItemStatus.REJECTED_BY_ARBITRATORS, EnumSet.of(DisputeItemStatus.CLAIM), ActionSide.FINANCIAL_SERVER, true, false, false) {
        @Override
        public void changeDisputeStatus(DisputeData disputeData) {
            changeDisputeStatusForPostClaimDisputeItem(disputeData);
        }
    };

    protected WebSocketService webSocketService;
    protected Transactions transactions;
    protected RollingReserveService rollingReserveService;
    private final DisputeItemStatus newDisputeItemStatus;
    private final Set<DisputeItemStatus> previousDisputeItemStatuses;
    private final ActionSide actionSide;
    private final boolean finalStatus;
    private final boolean refundable;
    private final boolean preClaim;

    private static class DisputeItemStatusServices {
        private static final Map<DisputeItemStatus, DisputeItemStatusService> disputeItemStatusServiceMap = new EnumMap<>(DisputeItemStatus.class);
    }

    DisputeItemStatusService(DisputeItemStatus newDisputeItemStatus, Set<DisputeItemStatus> previousDisputeItemStatuses, ActionSide actionSide, boolean finalStatus, boolean refundable, boolean preClaim) {
        this.newDisputeItemStatus = newDisputeItemStatus;
        DisputeItemStatusServices.disputeItemStatusServiceMap.put(newDisputeItemStatus, this);
        this.previousDisputeItemStatuses = previousDisputeItemStatuses;
        this.actionSide = actionSide;
        this.finalStatus = finalStatus;
        this.refundable = refundable;
        this.preClaim = preClaim;
    }

    public static DisputeItemStatusService getByDisputeItemStatus(DisputeItemStatus disputeItemStatus) {
        return DisputeItemStatusServices.disputeItemStatusServiceMap.get(disputeItemStatus);
    }

    private boolean isActionSideValid(ActionSide actionSide) {
        return this.actionSide.equals(actionSide);
    }

    public boolean isFinalStatus() {
        return finalStatus;
    }

    public boolean isRefundable() {
        return refundable;
    }

    public boolean isPreClaim() {
        return preClaim;
    }

    public void changeStatus(DisputeData disputeData, Long itemId, ActionSide actionSide) {
        if (!isActionSideValid(actionSide)) {
            throw new DisputeItemChangeStatusException(String.format(DISPUTE_ITEM_STATUS_INVALID_ACTIONSIDE, actionSide.toString(), toString()));
        }
        DisputeItemData disputeItemData = disputeData.getDisputeItem(itemId);
        if (disputeItemData == null) {
            throw new DisputeItemChangeStatusException(DISPUTE_ITEM_NOT_FOUND);
        }
        if (!disputeItemData.getStatus().equals(DisputeItemStatus.RECALL) && valueOf(disputeItemData.getStatus().toString()).isFinalStatus()) {
            throw new DisputeItemChangeStatusException(DISPUTE_ITEM_STATUS_FINAL);
        }
        if (!previousDisputeItemStatuses.contains(disputeItemData.getStatus())) {
            throw new DisputeItemChangeStatusException(DISPUTE_ITEM_STATUS_INVALID_CHANGE);
        }
        changeStatus(disputeItemData);
        webSocketService.notifyOnItemStatusChange(disputeData, itemId, actionSide);
        changeDisputeItemsStatuses(disputeData);

        changeDisputeStatus(disputeData);
    }

    private void changeStatus(DisputeItemData disputeItemData) {
        disputeItemData.setStatus(newDisputeItemStatus);
        if (actionSide.equals(ActionSide.ARBITRATOR)) {
            disputeItemData.setArbitratorsDecisionTime(Instant.now());
        }
    }

    public void changeDisputeItemsStatuses(DisputeData disputeData) {
        // implemented by sub classes
    }

    public void changePreClaimDisputeItemsStatuses(DisputeData disputeData) {
        if (existRecallDisputeItems(disputeData)) {
            return;
        }
        disputeData.getDisputeItems().forEach(disputeItemData -> {
            if (disputeItemData.getStatus().equals(DisputeItemStatus.REJECTED_BY_MERCHANT)) {
                disputeItemData.setStatus(DisputeItemStatus.CLAIM);
                webSocketService.notifyOnItemStatusChange(disputeData, disputeItemData.getId(), ActionSide.FINANCIAL_SERVER);
            }
        });
    }

    public boolean existRecallDisputeItems(DisputeData disputeData) {
        Stream<DisputeItemData> disputeItemDataStream = disputeData.getDisputeItems().stream().filter(disputeItemData -> disputeItemData.getStatus().equals(DisputeItemStatus.RECALL));
        return disputeItemDataStream.count() != 0;
    }

    public boolean existClaimDisputeItems(DisputeData disputeData) {
        Stream<DisputeItemData> disputeItemDataStream = disputeData.getDisputeItems().stream().filter(disputeItemData -> disputeItemData.getStatus().equals(DisputeItemStatus.CLAIM));
        return disputeItemDataStream.count() != 0;
    }

    public void changeDisputeStatusForPostClaimDisputeItem(DisputeData disputeData) {
        for (DisputeItemData disputeItemData : disputeData.getDisputeItems()) {
            if (disputeItemData.getStatus().equals(DisputeItemStatus.CLAIM)) {
                return;
            }
        }
        DisputeStatusService.CLOSED.changeStatus(disputeData);
    }

    public void createChargeBackTransaction(DisputeData disputeData) {
        List<DisputeItemData> refundableDisputeItems = disputeData.getDisputeItems().stream().filter(disputeItemData -> valueOf(disputeItemData.getStatus().toString()).isRefundable()).collect(Collectors.toList());
        if (!refundableDisputeItems.isEmpty()) {
            BigDecimal refundAmount = refundableDisputeItems.stream().map(DisputeItemData::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            TransactionData transactionData = transactions.getByHash(disputeData.getTransactionHash());
            rollingReserveService.chargebackConsumer(disputeData, transactionData.getSenderHash(), refundAmount);
        }

    }

    abstract void changeDisputeStatus(DisputeData disputeData);

    public boolean isFinalStatusForAllItems(DisputeData disputeData) {
        List<DisputeItemData> disputeItems = disputeData.getDisputeItems();
        for (DisputeItemData disputeItemData : disputeItems) {
            if (disputeItemData.getStatus().equals(DisputeItemStatus.RECALL) || !valueOf(disputeItemData.getStatus().toString()).isFinalStatus()) {
                return false;
            }
        }
        return true;
    }

    @Component
    public static class DisputeItemStatusServiceInjector {
        @Autowired
        private Transactions transactions;
        @Autowired
        private RollingReserveService rollingReserveService;
        @Autowired
        private WebSocketService webSocketService;

        @PostConstruct
        public void postConstruct() {
            for (DisputeItemStatusService disputeItemStatusService : EnumSet.allOf(DisputeItemStatusService.class)) {
                disputeItemStatusService.transactions = transactions;
                disputeItemStatusService.rollingReserveService = rollingReserveService;
                disputeItemStatusService.webSocketService = webSocketService;
            }
        }
    }
}

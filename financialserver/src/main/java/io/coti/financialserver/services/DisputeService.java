package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.PaymentInputBaseTransactionData;
import io.coti.basenode.data.PaymentItemData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Collection;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.financialserver.crypto.DisputeCrypto;
import io.coti.financialserver.crypto.GetDisputeHistoryCrypto;
import io.coti.financialserver.crypto.GetDisputesCrypto;
import io.coti.financialserver.data.*;
import io.coti.financialserver.http.*;
import io.coti.financialserver.http.data.GetDisputeHistoryData;
import io.coti.financialserver.http.data.GetDisputesData;
import io.coti.financialserver.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class DisputeService {

    private static final int COUNT_ARBITRATORS_PER_DISPUTE = 2;
    @Value("#{'${arbitrators.userHashes}'.split(',')}")
    private List<String> arbitratorUserHashes;
    @Autowired
    private GetDisputesCrypto getDisputesCrypto;
    @Autowired
    private DisputeCrypto disputeCrypto;
    @Autowired
    private GetDisputeHistoryCrypto getDisputeHistoryCrypto;
    @Autowired
    private Transactions transactions;
    @Autowired
    private Disputes disputes;
    @Autowired
    private ConsumerDisputes consumerDisputes;
    @Autowired
    private MerchantDisputes merchantDisputes;
    @Autowired
    private ArbitratorDisputes arbitratorDisputes;
    @Autowired
    private TransactionDisputes transactionDisputes;
    @Autowired
    private ReceiverBaseTransactionOwners receiverBaseTransactionOwners;
    @Autowired
    private DisputeHistory disputeHistory;
    @Autowired
    private DisputeEvents disputeEvents;
    @Autowired
    private UnreadUserDisputeEvents unreadUserDisputeEvents;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private WebSocketService webSocketService;
    private Map<ActionSide, Collection<UserDisputesData>> userDisputesCollectionMap = new EnumMap<>(ActionSide.class);

    @PostConstruct
    public void init() {
        userDisputesCollectionMap.put(ActionSide.Consumer, consumerDisputes);
        userDisputesCollectionMap.put(ActionSide.Merchant, merchantDisputes);
        userDisputesCollectionMap.put(ActionSide.Arbitrator, arbitratorDisputes);
    }

    public ResponseEntity<IResponse> createDispute(NewDisputeRequest newDisputeRequest) {

        DisputeData disputeData = newDisputeRequest.getDisputeData();

        if (!disputeCrypto.verifySignature(disputeData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        TransactionData transactionData = transactions.getByHash(disputeData.getTransactionHash());

        if (transactionData == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_TRANSACTION_NOT_FOUND, STATUS_ERROR));
        }

        disputeData.setTransactionCreationTime(transactionData.getCreateTime());

        PaymentInputBaseTransactionData paymentInputBaseTransactionData = transactionHelper.getPaymentInputBaseTransaction(transactionData);
        if (paymentInputBaseTransactionData == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_TRANSACTION_NOT_PAYMENT, STATUS_ERROR));
        }
        List<PaymentItemData> paymentItems = paymentInputBaseTransactionData.getItems();

        List<Long> itemIds = new ArrayList<>();
        BigDecimal disputeAmount = BigDecimal.ZERO;

        for (DisputeItemData item : disputeData.getDisputeItems()) {
            Supplier<Stream<PaymentItemData>> paymentItemsStreamSupplier = () -> paymentItems.stream().filter(paymentItemData -> paymentItemData.getItemId().equals(item.getId()));
            if (itemIds.contains(item.getId()) || paymentItemsStreamSupplier.get().count() == 0) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_ITEMS_INVALID, STATUS_ERROR));
            }
            PaymentItemData paymentItemData = paymentItemsStreamSupplier.get().findFirst().get();
            item.setPrice(paymentItemData.getItemPrice());
            item.setQuantity(paymentItemData.getItemQuantity());
            item.setName(paymentItemData.getItemName());

            disputeAmount = disputeAmount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
            itemIds.add(item.getId());
        }
        disputeData.setAmount(disputeAmount);

        if (!disputeData.getConsumerHash().equals(transactionData.getSenderHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_TRANSACTION_SENDER_INVALID, STATUS_ERROR));
        }

        if (isDisputeInProcessForTransactionHash(disputeData.getTransactionHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(OPEN_DISPUTE_IN_PROCESS_FOR_THIS_TRANSACTION, STATUS_ERROR));
        }

        if (isDisputeExistForTransaction(disputeData.getTransactionHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ALREADY_EXISTS_FOR_TRANSACTION, STATUS_ERROR));
        }

        Hash merchantHash = getMerchantHash(transactionHelper.getReceiverBaseTransactionHash(transactionData));
        if (merchantHash == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_MERCHANT_NOT_FOUND, STATUS_ERROR));
        }

        disputeData.setMerchantHash(merchantHash);
        disputeData.init();

        if (!areDisputeItemsAvailableForDispute(disputeData.getConsumerHash(), disputeData.getDisputeItems(), transactionData.getHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ITEMS_EXIST_ALREADY, STATUS_ERROR));
        }

        TransactionDisputesData transactionDisputesData = transactionDisputes.getByHash(disputeData.getTransactionHash());
        if (transactionDisputesData == null) {
            transactionDisputesData = new TransactionDisputesData();
            transactionDisputesData.setHash(disputeData.getTransactionHash());
        }
        transactionDisputesData.appendDisputeHash(disputeData.getHash());
        transactionDisputes.put(transactionDisputesData);

        addUserDisputeHash(ActionSide.Consumer, disputeData.getConsumerHash(), disputeData.getHash());
        addUserDisputeHash(ActionSide.Merchant, merchantHash, disputeData.getHash());

        disputes.put(disputeData);
        webSocketService.notifyOnNewDispute(disputeData);

        return ResponseEntity.status(HttpStatus.OK).body(new GetDisputesResponse(Arrays.asList(disputeData), ActionSide.Consumer, disputeData.getConsumerHash()));
    }

    private void addUserDisputeHash(ActionSide actionSide, Hash userHash, Hash disputeHash) {

        Collection<UserDisputesData> userDisputesCollection = userDisputesCollectionMap.get(actionSide);

        UserDisputesData userDisputesData = userDisputesCollection.getByHash(userHash);

        if (userDisputesData == null) {
            userDisputesData = new UserDisputesData();
            userDisputesData.setHash(userHash);
        }

        userDisputesData.appendDisputeHash(disputeHash);
        userDisputesCollection.put(userDisputesData);
    }

    public ResponseEntity<IResponse> getDisputes(GetDisputesRequest getDisputesRequest) {

        GetDisputesData getDisputesData = getDisputesRequest.getDisputesData();

        if (!getDisputesCrypto.verifySignature(getDisputesData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        Collection<UserDisputesData> userDisputesCollection = userDisputesCollectionMap.get(getDisputesData.getDisputeSide());
        if (userDisputesCollection == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_UNAUTHORIZED, STATUS_ERROR));
        }
        UserDisputesData userDisputesData = userDisputesCollection.getByHash(getDisputesData.getUserHash());

        if (userDisputesData == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_UNAUTHORIZED, STATUS_ERROR));
        }

        List<Hash> userDisputeHashes = userDisputesData.getDisputeHashes();

        if (getDisputesData.getDisputeHashes() == null) {
            getDisputesData.setDisputeHashes(userDisputesData.getDisputeHashes());
        }

        List<DisputeData> disputesData = new ArrayList<>();

        for (Hash disputeHash : getDisputesData.getDisputeHashes()) {
            DisputeData disputeData = disputes.getByHash(disputeHash);

            if (disputeData == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(disputeHash + " " + DISPUTE_NOT_FOUND, STATUS_ERROR));
            }

            if (!userDisputeHashes.contains(disputeHash)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_UNAUTHORIZED, STATUS_ERROR));
            }

            disputesData.add(disputeData);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new GetDisputesResponse(disputesData, getDisputesData.getDisputeSide(), getDisputesData.getUserHash()));
    }

    public ResponseEntity<IResponse> getDisputeHistory(GetDisputeHistoryRequest getDisputeHistoryRequest) {
        GetDisputeHistoryData getDisputeHistoryData = getDisputeHistoryRequest.getDisputeHistoryData();

        if (!getDisputeHistoryCrypto.verifySignature(getDisputeHistoryData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }

        List<DisputeEventResponse> disputeEventResponses = new ArrayList<>();

        DisputeHistoryData disputeHistoryData = disputeHistory.getByHash(getDisputeHistoryData.getDisputeHash());
        if (disputeHistoryData != null) {
            disputeHistoryData.getDisputeEventHashToEventDisplayUserMap().forEach((disputeEventHash, userHashToEventDisplaySideMap) -> {
                ActionSide eventDisplaySide = userHashToEventDisplaySideMap.get(getDisputeHistoryData.getUserHash());
                if (eventDisplaySide != null) {
                    boolean eventRead = true;
                    UnreadUserDisputeEventData unreadUserDisputeEventData = unreadUserDisputeEvents.getByHash(getDisputeHistoryData.getUserHash());
                    if (unreadUserDisputeEventData != null && unreadUserDisputeEventData.getDisputeEventHashToEventDisplaySideMap().get(disputeEventHash) != null) {
                        eventRead = false;
                    }
                    disputeEventResponses.add(new DisputeEventResponse(disputeEvents.getByHash(disputeEventHash), getDisputeHistoryData.getUserHash(), eventDisplaySide, eventRead));
                }
            });
        }

        return ResponseEntity.status(HttpStatus.OK).body(new GetDisputeHistoryResponse(disputeEventResponses));
    }

    public Boolean isAuthorizedDisputeDetailDisplay(DisputeData disputeData, Hash userHash) {

        return userHash.equals(disputeData.getConsumerHash()) || userHash.equals(disputeData.getMerchantHash()) || disputeData.getArbitratorHashes().contains(userHash);
    }

    public void update(DisputeData disputeData) {

        if (disputeData.getDisputeStatus().equals(DisputeStatus.Claim) && disputeData.getArbitratorHashes().isEmpty()) {
            assignToArbitrators(disputeData);
            disputeData.setArbitratorsAssignTime(Instant.now());
        }

        disputeData.setUpdateTime(Instant.now());
        disputes.put(disputeData);
    }

    public void updateAfterVote(DisputeData disputeData, DisputeItemData disputeItemData) throws Exception {

        int arbitratorsCount = disputeData.getArbitratorHashes().size();
        int majorityOfVotes = ((int) Math.floor(arbitratorsCount / 2)) + 1;

        int votesForConsumer = 0;
        int votesForMerchant = 0;

        List<DisputeItemVoteData> disputeItemVotes = disputeItemData.getDisputeItemVotesData();

        for (DisputeItemVoteData disputeItemVoteData : disputeItemVotes) {

            if (disputeItemVoteData.getStatus().equals(DisputeItemVoteStatus.AcceptedByArbitrator)) {
                votesForConsumer++;
            } else if (disputeItemVoteData.getStatus().equals(DisputeItemVoteStatus.RejectedByArbitrator)) {
                votesForMerchant++;
            }
        }

        if (votesForConsumer >= majorityOfVotes) {
            DisputeItemStatusService.AcceptedByArbitrators.changeStatus(disputeData, disputeItemData.getId(), ActionSide.FinancialServer);

        } else if (votesForMerchant >= majorityOfVotes || disputeItemVotes.size() == arbitratorsCount) {
            DisputeItemStatusService.RejectedByArbitrators.changeStatus(disputeData, disputeItemData.getId(), ActionSide.FinancialServer);
        }

        disputeData.setUpdateTime(Instant.now());
        disputes.put(disputeData);
    }

    private void assignToArbitrators(DisputeData disputeData) {

        int random;

        List<String> arbitratorUserHashes = new ArrayList<>(this.arbitratorUserHashes);
        for (int i = 0; i < COUNT_ARBITRATORS_PER_DISPUTE; i++) {

            random = (int) ((Math.random() * arbitratorUserHashes.size()));

            Hash arbitratorHash = new Hash(arbitratorUserHashes.get(random));
            disputeData.getArbitratorHashes().add(arbitratorHash);
            addUserDisputeHash(ActionSide.Arbitrator, arbitratorHash, disputeData.getHash());

            arbitratorUserHashes.remove(random);
        }

        webSocketService.notifyOnDisputeToArbitrators(disputeData);
    }

    private Hash getMerchantHash(Hash receiverBaseTransactionHash) {
        ReceiverBaseTransactionOwnerData receiverBaseTransactionOwnerData = receiverBaseTransactionOwners.getByHash(receiverBaseTransactionHash);

        if (receiverBaseTransactionOwnerData != null) {
            return receiverBaseTransactionOwnerData.getMerchantHash();
        }

        return null;
    }

    private boolean isDisputeInProcessForTransactionHash(Hash transactionHash) {
        TransactionDisputesData transactionDisputesData = transactionDisputes.getByHash(transactionHash);

        if (transactionDisputesData == null) {
            return false;
        }

        DisputeData disputeData;

        for (Hash disputeHash : transactionDisputesData.getDisputeHashes()) {
            disputeData = disputes.getByHash(disputeHash);
            if (disputeData.getDisputeStatus().equals(DisputeStatus.Recall) || !DisputeStatusService.valueOf(disputeData.getDisputeStatus().toString()).isFinalStatus()) {
                return true;
            }
        }

        return false;
    }

    private boolean isDisputeExistForTransaction(Hash transactionHash) {
        TransactionDisputesData transactionDisputesData = transactionDisputes.getByHash(transactionHash);

        return transactionDisputesData != null;
    }

    public boolean areDisputeItemsAvailableForDispute(Hash consumerHash, List<DisputeItemData> items, Hash transactionHash) {

        UserDisputesData userDisputesData = consumerDisputes.getByHash(consumerHash);

        if (userDisputesData == null || userDisputesData.getDisputeHashes() == null) {
            return true;
        }

        DisputeData disputeData;
        for (Hash disputeHash : userDisputesData.getDisputeHashes()) {
            disputeData = disputes.getByHash(disputeHash);

            for (DisputeItemData item : items) {
                if (disputeData.getDisputeItem(item.getId()) != null && disputeData.getTransactionHash().equals(transactionHash)) {
                    return false;
                }
            }
        }

        return true;
    }

    public ActionSide getActionSide(DisputeData disputeData, Hash actionInitiatorHash) {

        if (disputeData.getConsumerHash().equals(actionInitiatorHash)) {
            return ActionSide.Consumer;
        } else if (disputeData.getMerchantHash().equals(actionInitiatorHash)) {
            return ActionSide.Merchant;
        }

        return null;
    }
}

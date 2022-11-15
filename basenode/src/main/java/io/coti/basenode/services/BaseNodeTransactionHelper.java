package io.coti.basenode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static io.coti.basenode.data.TransactionState.*;

@Slf4j
@Service
public class BaseNodeTransactionHelper implements ITransactionHelper {

    public static final int CURRENCY_SCALE = 8;
    @Autowired
    private AddressTransactionsHistories addressTransactionsHistories;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IConfirmationService confirmationService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionIndexes transactionIndexes;
    @Autowired
    private ExpandedTransactionTrustScoreCrypto expandedTransactionTrustScoreCrypto;
    @Autowired
    private BaseNodeCurrencyService currencyService;
    @Autowired
    private IMintingService mintingService;
    @Autowired
    private IEventService eventService;
    @Autowired
    private ITransactionHelper transactionHelper;
    private Map<Hash, Stack<TransactionState>> transactionHashToTransactionStateStackMapping;
    private final AtomicLong totalTransactions = new AtomicLong(0);
    private Set<Hash> noneIndexedTransactionHashes;
    @Autowired
    private INetworkService networkService;
    @Autowired
    protected ITransactionPropagationCheckService transactionPropagationCheckService;

    private long totalNumberOfTransactionsFromRecovery = 0;
    private long totalNumberOfTransactionsFromLocal = 0;

    @PostConstruct
    private void init() {
        transactionHashToTransactionStateStackMapping = new ConcurrentHashMap<>();
        noneIndexedTransactionHashes = Sets.newConcurrentHashSet();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public boolean validateBaseTransactionAmounts(List<BaseTransactionData> baseTransactions) {
        Map<Hash, BigDecimal> transactionTotals = new HashMap<>();
        for (BaseTransactionData baseTransactionData : baseTransactions) {
            transactionTotals.put(baseTransactionData.getCurrencyHash(),
                    transactionTotals.getOrDefault(baseTransactionData.getCurrencyHash(), BigDecimal.ZERO).add(baseTransactionData.getAmount()));
        }
        return transactionTotals.values().stream().allMatch(t -> t.compareTo(BigDecimal.ZERO) == 0);
    }

    @Override
    public boolean validateBaseTransactionsDataIntegrity(TransactionData transactionData) {
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        return validateBaseTransactionAmounts(baseTransactions) && validateBaseTransactionTrustScoreNodeResults(transactionData);
    }

    @Override
    public boolean validateTransactionTimeFields(TransactionData transactionData) {
        Instant now = Instant.now();
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (!transactionTimeFieldValid(now, baseTransactionData.getCreateTime())) {
                return false;
            }
        }
        return transactionTimeFieldValid(now, transactionData.getCreateTime());
    }

    private boolean transactionTimeFieldValid(Instant systemTime, Instant timeField) {
        return timeField.isAfter(systemTime.minus(60, ChronoUnit.MINUTES)) && timeField.isBefore(systemTime.plus(10, ChronoUnit.MINUTES));
    }

    @Override
    public void updateAddressTransactionHistory(TransactionData transactionData) {
        transactionData.getBaseTransactions().forEach(baseTransactionData ->
                updateAddressTransactionsHistories(baseTransactionData.getAddressHash(), transactionData)
        );
        updateMintedAddress(transactionData);
    }

    public void updateMintedAddress(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = transactionHelper.getTokenMintingFeeData(transactionData);
        if (tokenMintingFeeBaseTransactionData != null) {
            Hash receiverAddressHash = tokenMintingFeeBaseTransactionData.getServiceData().getReceiverAddress();
            Optional<BaseTransactionData> identicalAddresses = transactionData.getBaseTransactions().stream().filter(t -> t.getAddressHash().equals(receiverAddressHash)).findFirst();
            if (!identicalAddresses.isPresent()) {
                updateAddressTransactionsHistories(receiverAddressHash, transactionData);
            }
        }
    }

    private void updateAddressTransactionsHistories(Hash addressHash, TransactionData transactionData) {
        AddressTransactionsHistory addressHistory = Optional.ofNullable(addressTransactionsHistories.getByHash(addressHash))
                .orElse(new AddressTransactionsHistory(addressHash));

        if (!addressHistory.addTransactionHashToHistory(transactionData.getHash())) {
            log.debug("Transaction {} is already in history of address {}", transactionData.getHash(), addressHash);
        }
        addressTransactionsHistories.put(addressHistory);
    }

    public void updateAddressTransactionHistory(Map<Hash, AddressTransactionsHistory> addressToTransactionsHistoryMap, TransactionData transactionData) {
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            AddressTransactionsHistory addressHistory;
            if (!addressToTransactionsHistoryMap.containsKey(baseTransactionData.getAddressHash())) {
                addressHistory = addressTransactionsHistories.getByHash(baseTransactionData.getAddressHash());
                if (addressHistory == null) {
                    addressHistory = new AddressTransactionsHistory(baseTransactionData.getAddressHash());
                }
            } else {
                addressHistory = addressToTransactionsHistoryMap.get(baseTransactionData.getAddressHash());
            }
            if (!addressHistory.addTransactionHashToHistory(transactionData.getHash())) {
                log.debug("Transaction {} is already in history of address {}", transactionData.getHash(), baseTransactionData.getAddressHash());
            }
            addressToTransactionsHistoryMap.put(baseTransactionData.getAddressHash(), addressHistory);
        });
        updateMintedAddress(transactionData);
    }

    public boolean validateTransactionCrypto(TransactionData transactionData) {
        return transactionCrypto.isTransactionValid(transactionData);

    }

    public boolean validateTransactionType(TransactionData transactionData) {
        Hash nativeCurrencyHash = currencyService.getNativeCurrencyHash();
        try {
            TransactionType transactionType = transactionData.getType();
            if (transactionType == null) {
                log.error("Transaction {} has null type", transactionData.getHash());
                return false;
            }

            return TransactionTypeValidation.getByType(transactionType).validateBaseTransactions(transactionData, nativeCurrencyHash);
        } catch (Exception e) {
            log.error("Validate transaction type error", e);
            return false;
        }
    }

    @Override
    public boolean validateBaseTransactionTrustScoreNodeResults(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (baseTransactionData instanceof ITrustScoreNodeValidatable && !validateBaseTransactionTrustScoreNodeResult((ITrustScoreNodeValidatable) baseTransactionData)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateBaseTransactionTrustScoreNodeResult(ITrustScoreNodeValidatable trustScoreNodeValidatable) {
        try {
            return isTrustScoreNodeResultValid(trustScoreNodeValidatable.getTrustScoreNodeResult());

        } catch (Exception e) {
            log.error("Validate base transaction trust score node result error", e);
            return false;
        }
    }

    private boolean isTrustScoreNodeResultValid(List<TrustScoreNodeResultData> trustScoreNodeResult) {
        if (trustScoreNodeResult.size() != 3) {
            return false;
        }
        int validNumber = 0;
        for (TrustScoreNodeResultData trustScoreNodeResultData : trustScoreNodeResult) {
            validNumber += trustScoreNodeResultData.isValid() ? 1 : 0;
        }
        return validNumber >= 2;
    }

    public boolean isTransactionExists(TransactionData transactionData) {
        return isTransactionHashExists(transactionData.getHash());
    }

    public boolean isTransactionHashExists(Hash transactionHash) {
        return isTransactionHashProcessing(transactionHash) || isTransactionHashInDB(transactionHash);
    }

    private boolean isTransactionHashInDB(Hash transactionHash) {
        return transactions.getByHash(transactionHash) != null;
    }

    @Override
    public boolean isTransactionHashProcessing(Hash transactionHash) {
        return transactionHashToTransactionStateStackMapping.containsKey(transactionHash);
    }

    public boolean isTransactionAlreadyPropagated(TransactionData transactionData) {
        if (isTransactionExists(transactionData)) {
            if (!isTransactionHashProcessing(transactionData.getHash())) {
                addDspResultToDb(transactionData.getDspConsensusResult());
            }
            return true;
        }
        return false;
    }

    private void addDspResultToDb(DspConsensusResult dspConsensusResult) {
        if (dspConsensusResult == null) {
            return;
        }
        if (transactionIndexes.getByHash(new Hash(dspConsensusResult.getIndex())) == null) {
            confirmationService.setDspcToTrue(dspConsensusResult);
        }
    }

    public boolean validateTrustScore(TransactionData transactionData) {
        Hash transactionHash = transactionData.getHash();
        Hash senderHash = transactionData.getSenderHash();
        List<TransactionTrustScoreData> transactionTrustScores = transactionData.getTrustScoreResults();
        if (transactionTrustScores == null) {
            return false;
        }
        Map<Double, Integer> trustScoreResults = new HashMap<>();
        Set<Hash> transactionTrustScoreNodes = new HashSet<>();
        for (TransactionTrustScoreData transactionTrustScoreData : transactionTrustScores) {
            ExpandedTransactionTrustScoreData expandedTransactionTrustScoreData = new ExpandedTransactionTrustScoreData(senderHash, transactionHash, transactionTrustScoreData);
            if (transactionTrustScoreNodes.contains(transactionTrustScoreData.getTrustScoreNodeHash()) ||
                    !expandedTransactionTrustScoreCrypto.verifySignature(expandedTransactionTrustScoreData)) {
                return false;
            }
            Double transactionTrustScore = transactionTrustScoreData.getTrustScore();
            trustScoreResults.computeIfPresent(transactionTrustScore, (trustScore, currentAmount) -> currentAmount + 1);
            trustScoreResults.putIfAbsent(transactionTrustScore, 1);
            transactionTrustScoreNodes.add(transactionTrustScoreData.getTrustScoreNodeHash());
        }
        transactionData.setSenderTrustScore(Collections.max(trustScoreResults.entrySet(), Map.Entry.comparingByValue()).getKey());
        return true;
    }

    public void startHandleTransaction(TransactionData transactionData) {
        transactionHashToTransactionStateStackMapping.put(transactionData.getHash(), new Stack<>());
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(RECEIVED);
    }

    public void endHandleTransaction(TransactionData transactionData) {
        if (!isTransactionHashProcessing(transactionData.getHash())) {
            return;
        }
        if (isTransactionFinished(transactionData)) {
            log.debug("Transaction {} handled successfully", transactionData.getHash());
        } else {
            rollbackTransaction(transactionData);
        }
        transactionHashToTransactionStateStackMapping.remove(transactionData.getHash());
    }

    public void continueHandleRejectedTransaction(TransactionData rejectedTransactionData) {
        detachTransactionFromCluster(rejectedTransactionData);
        revertPreBalance(rejectedTransactionData);
        revertPayloadAction(rejectedTransactionData);
        revertSavedInDB(rejectedTransactionData);
    }

    @Override
    public boolean isTransactionFinished(TransactionData transactionData) {
        return isTransactionHashProcessing(transactionData.getHash()) && transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).peek().equals(FINISHED);
    }

    private void rollbackTransaction(TransactionData transactionData) {
        Stack<TransactionState> currentTransactionStateStack = transactionHashToTransactionStateStackMapping.get(transactionData.getHash());
        while (!currentTransactionStateStack.isEmpty()) {
            TransactionState transactionState = currentTransactionStateStack.pop();
            switch (transactionState) {
                case PRE_BALANCE_CHANGED:
                    revertPreBalance(transactionData);
                    break;
                case PAYLOAD_CHECKED:
                    revertPayloadAction(transactionData);
                    break;
                case SAVED_IN_DB:
                    revertSavedInDB(transactionData);
                    break;
                case RECEIVED:
                    break;
                default:
                    log.error("Transaction {} has a state {} which is illegal in rollback scenario", transactionData, transactionState);
                    throw new IllegalArgumentException("Invalid transaction state");
            }
        }
    }

    private void revertPayloadAction(TransactionData transactionData) {
        if (transactionData.getType() == TransactionType.TokenMinting) {
            log.error("Reverting minting transaction: {}", transactionData.getHash());
            mintingService.revertMintingAllocation(transactionData);
        }
        if (transactionData.getType() == TransactionType.TokenGeneration) {
            log.error("Reverting token generation transaction: {}", transactionData.getHash());
            currencyService.revertCurrencyUnconfirmedRecord(transactionData);
        }
    }

    private void revertSavedInDB(TransactionData transactionData) {
        log.error("Reverting transaction saved in DB: {}", transactionData.getHash());
        transactions.deleteByHash(transactionData.getHash());
        totalTransactions.decrementAndGet();
    }

    private void revertPreBalance(TransactionData transactionData) {
        log.error("Reverting pre balance: {}", transactionData.getHash());
        balanceService.rollbackBaseTransactions(transactionData);
    }

    public boolean checkBalancesAndAddToPreBalance(TransactionData transactionData) {
        if (!balanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions())) {
            return false;
        }
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(PRE_BALANCE_CHANGED);
        return true;
    }

    @Override
    public boolean checkTokenMintingAndAddToAllocatedAmount(TransactionData transactionData) {
        if (!mintingService.checkMintingAmountAndUpdateMintableAmount(transactionData)) {
            return false;
        }
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(PAYLOAD_CHECKED);
        return true;
    }

    @Override
    public boolean checkEventHardForkAndAddToEvents(TransactionData transactionData) {
        if (!eventService.checkEventAndUpdateEventsTable(transactionData)) {
            return false;
        }
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(PAYLOAD_CHECKED);
        return true;
    }

    @Override
    public boolean validateCurrencyUniquenessAndAddUnconfirmedRecord(TransactionData transactionData) {
        if (!currencyService.validateCurrencyUniquenessAndAddUnconfirmedRecord(transactionData)) {
            return false;
        }
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(PAYLOAD_CHECKED);
        return true;
    }

    public void attachTransactionToCluster(TransactionData transactionData) {
        transactionData.setTrustChainConsensus(false);
        transactionData.setTrustChainTrustScore(0);
        transactionData.setTransactionConsensusUpdateTime(null);
        transactionData.setChildrenTransactionHashes(new ArrayList<>());
        transactions.put(transactionData);
        totalTransactions.incrementAndGet();
        if (!isDspConfirmed(transactionData)) {
            addNoneIndexedTransaction(transactionData);
        }
        if (transactionData.getDspConsensusResult() != null) {
            confirmationService.setDspcToTrue(transactionData.getDspConsensusResult());
        }
        updateAddressTransactionHistory(transactionData);
        clusterService.attachToCluster(transactionData);
    }

    public void detachTransactionFromCluster(TransactionData transactionData) {
        transactionPropagationCheckService.removeTransactionHashFromUnconfirmed(transactionData.getHash());
        clusterService.detachFromCluster(transactionData);
        removeAddressTransactionHistory(transactionData);
        removeNoneIndexedTransaction(transactionData);
    }

    @Override
    public void updateTransactionOnCluster(TransactionData transactionData) {
        clusterService.updateTransactionOnTrustChainConfirmationCluster(transactionData);
    }

    public void setTransactionStateToSaved(TransactionData transactionData) {
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(SAVED_IN_DB);
    }

    public void setTransactionStateToFinished(TransactionData transactionData) {
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(FINISHED);
    }

    @Override
    public boolean isConfirmed(TransactionData transactionData) {
        return transactionData.isTrustChainConsensus() && isDspConfirmed(transactionData);
    }

    @Override
    public boolean isDspConfirmed(TransactionData transactionData) {
        return transactionData.getDspConsensusResult() != null && transactionData.getDspConsensusResult().isDspConsensus() && transactionIndexes.getByHash(new Hash(transactionData.getDspConsensusResult().getIndex())) != null;
    }

    @Override
    public Hash getReceiverBaseTransactionAddressHash(TransactionData transactionData) {

        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (baseTransactionData instanceof ReceiverBaseTransactionData) {
                return baseTransactionData.getAddressHash();
            }
        }
        return null;
    }

    @Override
    public Hash getReceiverBaseTransactionHash(TransactionData transactionData) {

        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (baseTransactionData instanceof ReceiverBaseTransactionData) {
                return baseTransactionData.getHash();
            }
        }
        return null;
    }

    @Override
    public BigDecimal getRollingReserveAmount(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (baseTransactionData instanceof RollingReserveData) {
                return baseTransactionData.getAmount();
            }
        }
        return null;
    }

    @Override
    public PaymentInputBaseTransactionData getPaymentInputBaseTransaction(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (baseTransactionData instanceof PaymentInputBaseTransactionData) {
                return (PaymentInputBaseTransactionData) baseTransactionData;
            }
        }
        return null;
    }

    @Override
    public TokenGenerationFeeBaseTransactionData getTokenGenerationFeeData(TransactionData tokenGenerationTransaction) {
        return (TokenGenerationFeeBaseTransactionData) tokenGenerationTransaction
                .getBaseTransactions()
                .stream()
                .filter(TokenGenerationFeeBaseTransactionData.class::isInstance)
                .findFirst().orElse(null);
    }

    @Override
    public TokenMintingFeeBaseTransactionData getTokenMintingFeeData(TransactionData tokenMintingTransaction) {
        return (TokenMintingFeeBaseTransactionData) tokenMintingTransaction
                .getBaseTransactions()
                .stream()
                .filter(TokenMintingFeeBaseTransactionData.class::isInstance)
                .findFirst().orElse(null);
    }

    @Override
    public long getTotalTransactions() {
        return totalTransactions.get();
    }

    @Override
    public long incrementTotalTransactions() {
        return totalTransactions.incrementAndGet();
    }

    @Override
    public void addNoneIndexedTransaction(TransactionData transactionData) {
        noneIndexedTransactionHashes.add(transactionData.getHash());
    }

    @Override
    public void removeNoneIndexedTransaction(TransactionData transactionData) {
        noneIndexedTransactionHashes.remove(transactionData.getHash());
    }

    @Override
    public Set<Hash> getNoneIndexedTransactionHashes() {
        return new HashSet<>(noneIndexedTransactionHashes);
    }

    @Override
    public EventInputBaseTransactionData getEventInputBaseTransactionData(TransactionData eventTransactionData) {
        return (EventInputBaseTransactionData) eventTransactionData
                .getBaseTransactions()
                .stream()
                .filter(EventInputBaseTransactionData.class::isInstance)
                .findFirst().orElse(null);
    }

    @Override
    public BigDecimal getNativeAmount(TransactionData transactionData) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        for (BaseTransactionData baseTransaction : baseTransactions) {
            if (currencyService.isNativeCurrency(baseTransaction.getCurrencyHash())) {
                totalAmount = totalAmount.add(baseTransaction.getAmount().signum() > 0 ? baseTransaction.getAmount() : BigDecimal.ZERO);
            }
        }
        return totalAmount;
    }

    @Override
    public TransactionData createNewTransaction(List<BaseTransactionData> baseTransactions, Hash transactionHash,
                                                String transactionDescription,
                                                List<TransactionTrustScoreData> trustScoreResults, Instant createTime,
                                                Hash senderHash, SignatureData senderSignature, TransactionType type) {
        TransactionData transactionData = new TransactionData(
                baseTransactions,
                transactionHash,
                transactionDescription,
                trustScoreResults,
                createTime,
                senderHash,
                senderSignature,
                type);

        transactionData.setAmount(getNativeAmount(transactionData));
        return transactionData;
    }

    @Override
    public TransactionData createNewTransaction(List<BaseTransactionData> baseTransactions, String transactionDescription, double senderTrustScore, Instant createTime, TransactionType type) {
        TransactionData transactionData = new TransactionData(baseTransactions, transactionDescription, senderTrustScore, createTime, type);

        transactionData.setAmount(getNativeAmount(transactionData));
        return transactionData;
    }

    @Override
    public boolean validateBaseTransactionPublicKey(BaseTransactionData baseTransactionData, NodeType nodeType) {
        try {
            boolean verified = false;
            List<Hash> nodesHashes = networkService.getNodesHashes(nodeType);
            Iterator<io.coti.basenode.data.Hash> nodeHashIterator = nodesHashes.iterator();
            while (nodeHashIterator.hasNext() && !verified) {
                Hash nodeHash = nodeHashIterator.next();
                String baseTransactionSignaturePublicKey = BaseTransactionCrypto.getByBaseTransactionClass(baseTransactionData.getClass()).getPublicKey(baseTransactionData);
                verified = nodeHash.toString().equals(baseTransactionSignaturePublicKey);
            }
            return verified;
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return false;
        }
    }

    @Override
    public void removeAddressTransactionHistory(TransactionData transactionData) {
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            Hash addressHash = baseTransactionData.getAddressHash();
            AddressTransactionsHistory addressHistory = addressTransactionsHistories.getByHash(addressHash);
            if (addressHistory == null) {
                log.error("Address history does not exist for address {}", addressHash);
            } else {
                if (!addressHistory.removeTransactionHashFromHistory(transactionData.getHash())) {
                    log.error("Transaction {} is not in history of address {}", transactionData.getHash(), addressHash);
                } else {
                    addressTransactionsHistories.put(addressHistory);
                }
            }
        });
    }


    @Override
    public long getTotalNumberOfTransactionsFromRecovery() {
        return totalNumberOfTransactionsFromRecovery;
    }

    @Override
    public long getTotalNumberOfTransactionsFromLocal() {
        return totalNumberOfTransactionsFromLocal;
    }

    @Override
    public void handleReportedTransactionsState(TransactionsStateData transactionsStateData) {
        totalNumberOfTransactionsFromLocal = getTotalTransactions();
        if ( transactionsStateData.getTransactionsAmount() > totalNumberOfTransactionsFromRecovery ) {
            totalNumberOfTransactionsFromRecovery = transactionsStateData.getTransactionsAmount();
        }
    }
}

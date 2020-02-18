package io.coti.basenode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
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
public class TransactionHelper implements ITransactionHelper {

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
    private Map<Hash, Stack<TransactionState>> transactionHashToTransactionStateStackMapping;
    private AtomicLong totalTransactions = new AtomicLong(0);
    private Set<Hash> noneIndexedTransactionHashes;

    @PostConstruct
    private void init() {
        transactionHashToTransactionStateStackMapping = new ConcurrentHashMap<>();
        noneIndexedTransactionHashes = Sets.newConcurrentHashSet();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public boolean validateBaseTransactionAmounts(List<BaseTransactionData> baseTransactions) {
        BigDecimal totalTransactionSum = BigDecimal.ZERO;
        for (BaseTransactionData baseTransactionData :
                baseTransactions) {
            totalTransactionSum = totalTransactionSum.add(baseTransactionData.getAmount());
        }
        return totalTransactionSum.compareTo(BigDecimal.ZERO) == 0;
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
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            AddressTransactionsHistory addressHistory = addressTransactionsHistories.getByHash(baseTransactionData.getAddressHash());

            if (addressHistory == null) {
                addressHistory = new AddressTransactionsHistory(baseTransactionData.getAddressHash());
            }
            if (!addressHistory.addTransactionHashToHistory(transactionData.getHash())) {
                log.debug("Transaction {} is already in history of address {}", transactionData.getHash(), baseTransactionData.getAddressHash());
            }
            addressTransactionsHistories.put(addressHistory);
        });
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
    }

    public boolean validateTransactionCrypto(TransactionData transactionData) {
        return transactionCrypto.isTransactionValid(transactionData);

    }

    public boolean validateTransactionType(TransactionData transactionData) {
        try {
            TransactionType transactionType = transactionData.getType();
            if (transactionType == null) {
                log.error("Transaction {} has null type", transactionData.getHash());
                return false;
            }

            return TransactionTypeValidation.valueOf(transactionType.toString()).validateBaseTransactions(transactionData);
        } catch (Exception e) {
            log.error("Validate transaction type error", e);
            return false;
        }
    }

    @Override
    public boolean validateBaseTransactionTrustScoreNodeResults(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (ITrustScoreNodeValidatable.class.isAssignableFrom(baseTransactionData.getClass()) && !validateBaseTransactionTrustScoreNodeResult((ITrustScoreNodeValidatable) baseTransactionData)) {
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
        if (transactionTrustScores == null)
            return false;
        Map<Double, Integer> trustScoreResults = new HashMap<>();
        Set<Hash> transactionTrustScoreNodes = new HashSet<>();
        for (TransactionTrustScoreData transactionTrustScoreData : transactionTrustScores) {
            ExpandedTransactionTrustScoreData expandedTransactionTrustScoreData = new ExpandedTransactionTrustScoreData(senderHash, transactionHash, transactionTrustScoreData);
            if (transactionTrustScoreNodes.contains(transactionTrustScoreData.getTrustScoreNodeHash()) ||
                    !expandedTransactionTrustScoreCrypto.verifySignature(expandedTransactionTrustScoreData))
                return false;
            Double transactionTrustScore = transactionTrustScoreData.getTrustScore();
            trustScoreResults.computeIfPresent(transactionTrustScore, (trustScore, currentAmount) -> currentAmount + 1);
            trustScoreResults.putIfAbsent(transactionTrustScore, 1);
            transactionTrustScoreNodes.add(transactionTrustScoreData.getTrustScoreNodeHash());
        }
        transactionData.setSenderTrustScore(Collections.max(trustScoreResults.entrySet(), Map.Entry.comparingByValue()).getKey());
        return true;
    }

    public void startHandleTransaction(TransactionData transactionData) {

        transactionHashToTransactionStateStackMapping.put(transactionData.getHash(), new Stack());
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(RECEIVED);
    }

    public void endHandleTransaction(TransactionData transactionData) {
        if (!transactionHashToTransactionStateStackMapping.containsKey(transactionData.getHash())) {
            return;
        }
        if (isTransactionFinished(transactionData)) {
            log.debug("Transaction {} handled successfully", transactionData.getHash());
        } else {
            rollbackTransaction(transactionData);
        }
        transactionHashToTransactionStateStackMapping.remove(transactionData.getHash());
    }

    @Override
    public boolean isTransactionFinished(TransactionData transactionData) {
        return transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).peek().equals(FINISHED);
    }

    private void rollbackTransaction(TransactionData transactionData) {
        Stack<TransactionState> currentTransactionStateStack = transactionHashToTransactionStateStackMapping.get(transactionData.getHash());
        while (!currentTransactionStateStack.isEmpty()) {
            TransactionState transactionState = currentTransactionStateStack.pop();
            switch (transactionState) {
                case PRE_BALANCE_CHANGED:
                    revertPreBalance(transactionData);
                    break;
                case SAVED_IN_DB:
                    revertSavedInDB(transactionData);
                    break;
                case RECEIVED:
                    transactionHashToTransactionStateStackMapping.remove(transactionData.getHash());
                    break;
                default: {
                    log.error("Transaction {} has a state {} which is illegal in rollback scenario", transactionData, transactionState);
                    throw new IllegalArgumentException("Invalid transaction state");
                }
            }
        }
    }

    private void revertSavedInDB(TransactionData transactionData) {
        log.error("Reverting transaction saved in DB: {}", transactionData.getHash());
    }

    private void revertPreBalance(TransactionData transactionData) {
        log.error("Reverting pre balance: {}", transactionData.getHash());
        balanceService.rollbackBaseTransactions(transactionData);
    }

    public boolean checkBalancesAndAddToPreBalance(TransactionData transactionData) {
        if (!isTransactionExists(transactionData)) {
            return false;
        }
        if (!balanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions())) {
            return false;
        }
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(PRE_BALANCE_CHANGED);
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
}
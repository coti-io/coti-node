package io.coti.basenode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import io.coti.basenode.http.GetTransactionBatchResponse;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.coti.basenode.data.TransactionState.*;

@Slf4j
@Service
public class TransactionHelper implements ITransactionHelper {

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
    private TransactionIndexService transactionIndexService;
    @Autowired
    private DspConsensusCrypto dspConsensusCrypto;
    @Autowired
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;
    @Autowired
    private LiveViewService liveViewService;
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

    public boolean validateBaseTransactionsDataIntegrity(TransactionData transactionData) {
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        return validateBaseTransactionAmounts(baseTransactions) && validateBaseTransactionTrustScoreNodeResults(transactionData);
    }

    private void updateAddressTransactionHistory(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            AddressTransactionsHistory addressHistory = addressTransactionsHistories.getByHash(baseTransactionData.getAddressHash());

            if (addressHistory == null) {
                addressHistory = new AddressTransactionsHistory(baseTransactionData.getAddressHash());
            }
            addressHistory.addTransactionHashToHistory(transactionData.getHash());
            addressTransactionsHistories.put(addressHistory);
        }
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
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean validateBaseTransactionTrustScoreNodeResults(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (ITrustScoreNodeValidatable.class.isAssignableFrom(baseTransactionData.getClass()) && validateBaseTransactionTrustScoreNodeResult((ITrustScoreNodeValidatable) baseTransactionData) == false) {
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
            e.printStackTrace();
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
        if (isTransactionHashProcessing(transactionHash)) {
            return true;
        }
        if (isTransactionHashInDB(transactionHash)) {
            return true;
        }
        return false;
    }

    private boolean isTransactionHashInDB(Hash transactionHash) {
        return transactions.getByHash(transactionHash) != null;
    }

    @Override
    public boolean isTransactionHashProcessing(Hash transactionHash) {
        return transactionHashToTransactionStateStackMapping.containsKey(transactionHash);
    }

    public boolean isTransactionAlreadyPropagated(TransactionData transactionData) {
        synchronized (transactionData) {
            if (isTransactionExists(transactionData)) {
                if (!isTransactionHashProcessing(transactionData.getHash())) {
                    addDspResultToDb(transactionData.getDspConsensusResult());
                }
                return true;
            }
            return false;
        }
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
        List<TransactionTrustScoreData> transactionTrustScores = transactionData.getTrustScoreResults();
        if (transactionTrustScores == null)
            return false;
        Map<Double, Integer> trustScoreResults = new HashMap<>();
        Set<Hash> transactionTrustScoreNodes = new HashSet<>();
        for (TransactionTrustScoreData transactionTrustScoreData : transactionTrustScores) {
            if (transactionTrustScoreNodes.contains(transactionTrustScoreData.getSignerHash()) || !transactionTrustScoreData.getTransactionHash().equals(transactionHash) ||
                    !transactionTrustScoreCrypto.verifySignature(transactionTrustScoreData))
                return false;
            Double transactionTrustScore = transactionTrustScoreData.getTrustScore();
            trustScoreResults.computeIfPresent(transactionTrustScore, (trustScore, currentAmount) -> currentAmount + 1);
            trustScoreResults.putIfAbsent(transactionTrustScore, 1);
            transactionTrustScoreNodes.add(transactionTrustScoreData.getSignerHash());
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
        if (transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).peek().equals(FINISHED)) {
            log.debug("Transaction {} handled successfully", transactionData.getHash());
        } else {
            rollbackTransaction(transactionData);
        }

        synchronized (transactionData) {
            transactionHashToTransactionStateStackMapping.remove(transactionData.getHash());
        }
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
        transactionData.setTrustChainTransactionHashes(new Vector<>());
        transactionData.setTrustChainTrustScore(0);
        transactionData.setTransactionConsensusUpdateTime(null);
        transactionData.setChildrenTransactionHashes(new ArrayList<>());
        transactions.put(transactionData);
        totalTransactions.incrementAndGet();
        if (transactionData.getDspConsensusResult() == null) {
            addNoneIndexedTransaction(transactionData);
        } else {
            confirmationService.setDspcToTrue(transactionData.getDspConsensusResult());
        }
        updateAddressTransactionHistory(transactionData);
        liveViewService.addTransaction(transactionData);
        clusterService.attachToCluster(transactionData);
    }

    public void setTransactionStateToSaved(TransactionData transactionData) {
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(SAVED_IN_DB);
    }

    public void setTransactionStateToFinished(TransactionData transactionData) {
        transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(FINISHED);
    }

    @Override
    public boolean handleVoteConclusionResult(DspConsensusResult dspConsensusResult) {
        if (!dspConsensusCrypto.verifySignature(dspConsensusResult)) {
            log.error("DspConsensus signature verification failed for transaction", dspConsensusResult.getHash());
            return false;
        }
        TransactionData transactionData = transactions.getByHash(dspConsensusResult.getHash());
        if (transactionData == null) {
            log.error("DspConsensus result is for a non-existing transaction: {}", dspConsensusResult.getHash());
            return false;
        }
        if (transactionData.getDspConsensusResult() != null) {
            log.error("DspConsensus result already exists for transaction: {}", dspConsensusResult.getHash());
            return false;
        }
        if (dspConsensusResult.isDspConsensus()) {
            log.debug("Valid vote conclusion received for transaction: {}", dspConsensusResult.getHash());
        } else {
            log.debug("Invalid vote conclusion received for transaction: {}", dspConsensusResult.getHash());
        }

        log.debug("DspConsensus result for transaction: Hash= {}, DspVoteResult= {}, Index= {}", dspConsensusResult.getHash(), dspConsensusResult.isDspConsensus(), dspConsensusResult.getIndex());

        return true;
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
    public long getTotalTransactions() {
        return totalTransactions.get();
    }

    @Override
    public long incrementTotalTransactions() {
        return totalTransactions.incrementAndGet();
    }

    @Override
    public GetTransactionBatchResponse getTransactionBatch(long startingIndex) {
        List<TransactionData> transactionsToSend = new LinkedList<>();
        if (startingIndex > transactionIndexService.getLastTransactionIndexData().getIndex()) {
            return new GetTransactionBatchResponse(transactionsToSend);
        }
        for (long i = startingIndex; i <= transactionIndexService.getLastTransactionIndexData().getIndex(); i++) {
            transactionsToSend.add(transactions.getByHash(transactionIndexes.getByHash(new Hash(i)).getTransactionHash()));
        }
        transactionsToSend.addAll(noneIndexedTransactionHashes.stream().map(hash -> transactions.getByHash(hash)).collect(Collectors.toList()));
        transactionsToSend.sort(Comparator.comparing(transactionData -> transactionData.getAttachmentTime()));
        return new GetTransactionBatchResponse(transactionsToSend);
    }

    public void addNoneIndexedTransaction(TransactionData transactionData) {
        noneIndexedTransactionHashes.add(transactionData.getHash());
    }

    public void removeNoneIndexedTransaction(TransactionData transactionData) {
        noneIndexedTransactionHashes.remove(transactionData.getHash());
    }

    public PaymentInputBaseTransactionData getPaymentInputBaseTransaction(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (baseTransactionData instanceof PaymentInputBaseTransactionData) {
                return (PaymentInputBaseTransactionData) baseTransactionData;
            }
        }
        return null;
    }
}
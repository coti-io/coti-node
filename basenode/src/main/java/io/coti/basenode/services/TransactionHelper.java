package io.coti.basenode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.TransactionCryptoWrapper;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.AddTransactionResponse;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetTransactionBatchResponse;
import io.coti.basenode.http.GetTransactionResponse;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.coti.basenode.data.TransactionState.*;
import static io.coti.basenode.http.HttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.http.HttpStringConstants.TRANSACTION_DOESNT_EXIST_MESSAGE;

@Slf4j
@Service
public class TransactionHelper implements ITransactionHelper {

    @Autowired
    private AddressTransactionsHistories addressTransactionsHistories;
    @Autowired
    private IBalanceService balanceService;
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
    private Map<Hash, Stack<TransactionState>> transactionHashToTransactionStateStackMapping;
    private AtomicLong totalTransactions = new AtomicLong(0);
    private Set<Hash> noneIndexedTransactionHashes;

    @PostConstruct
    private void init() {
        transactionHashToTransactionStateStackMapping = new ConcurrentHashMap<>();
        noneIndexedTransactionHashes = Sets.newConcurrentHashSet();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public boolean isLegalBalance(List<BaseTransactionData> baseTransactions) {
        BigDecimal totalTransactionSum = BigDecimal.ZERO;
        for (BaseTransactionData baseTransactionData :
                baseTransactions) {
            totalTransactionSum = totalTransactionSum.add(baseTransactionData.getAmount());
        }
        return totalTransactionSum.compareTo(BigDecimal.ZERO) == 0;
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

    public boolean validateTransaction(TransactionData transactionData) {
        TransactionCryptoWrapper verifyTransaction = new TransactionCryptoWrapper(transactionData);
        return verifyTransaction.isTransactionValid();
    }

    public boolean isTransactionExists(TransactionData transactionData) {
        if (transactionHashToTransactionStateStackMapping.containsKey(transactionData.getHash())) {
            return true;
        }
        if (transactions.getByHash(transactionData.getHash()) != null) {
            return true;
        }
        return false;
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

    public boolean startHandleTransaction(TransactionData transactionData) {
        synchronized (transactionData) {
            if (isTransactionExists(transactionData)) {
                return false;
            }
            transactionHashToTransactionStateStackMapping.put(transactionData.getHash(), new Stack());
            transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).push(RECEIVED);
            return true;
        }
    }

    public void endHandleTransaction(TransactionData transactionData) {
        if (!transactionHashToTransactionStateStackMapping.containsKey(transactionData.getHash())) {
            return;
        }
        if (transactionHashToTransactionStateStackMapping.get(transactionData.getHash()).peek() == FINISHED) {
            log.debug("Transaction handled successfully...");
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
            switch (currentTransactionStateStack.pop()) {
                case PRE_BALANCE_CHANGED:
                    revertPreBalance(transactionData);
                    break;
                case SAVED_IN_DB:
                    revertSavedInDB(transactionData);
                    break;
                case RECEIVED:
                    transactionHashToTransactionStateStackMapping.remove(transactionData.getHash());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid transaction state");
            }
        }
    }

    private void revertSavedInDB(TransactionData transactionData) {
        log.error("Reverting transaction saved in DB");
    }

    private void revertPreBalance(TransactionData transactionData) {
        log.error("Reverting pre balance...");
        balanceService.rollbackBaseTransactions(transactionData);
    }

    public ResponseEntity<BaseResponse> getTransactionDetails(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            TRANSACTION_DOESNT_EXIST_MESSAGE));
        TransactionResponseData transactionResponseData = new TransactionResponseData(transactionData);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetTransactionResponse(transactionResponseData));
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
        totalTransactions.incrementAndGet();
        transactionData.setTrustChainConsensus(false);
        transactionData.setTrustChainTransactionHashes(new LinkedList<>());
        transactionData.setTrustChainTrustScore(0);
        transactionData.setTransactionConsensusUpdateTime(null);
        transactionData.setChildrenTransactions(new LinkedList<>());
        transactions.put(transactionData);
        if (transactionData.getDspConsensusResult() == null) {
            addNoneIndexedTransaction(transactionData);
        } else {
            balanceService.setDspcToTrue(transactionData.getDspConsensusResult());
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
        return transactionData.getDspConsensusResult() != null && transactionData.getDspConsensusResult().isDspConsensus();
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

        for (long i = startingIndex; i <= transactionIndexService.getLastTransactionIndexData().getIndex(); i++) {
            transactionsToSend.add(transactions.getByHash(transactionIndexes.getByHash(new Hash(i)).getTransactionHash()));
        }
        transactionsToSend.addAll(noneIndexedTransactionHashes.stream().map(hash -> transactions.getByHash(hash)).collect(Collectors.toList()));
        return new GetTransactionBatchResponse(transactionsToSend);
    }

    public void addNoneIndexedTransaction(TransactionData transactionData) {
        noneIndexedTransactionHashes.add(transactionData.getHash());
    }

    public void removeNoneIndexedTransaction(TransactionData transactionData) {
        noneIndexedTransactionHashes.remove(transactionData.getHash());
    }
}
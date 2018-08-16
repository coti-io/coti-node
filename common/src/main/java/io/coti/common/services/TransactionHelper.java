package io.coti.common.services;

import io.coti.common.crypto.DspConsensusCrypto;
import io.coti.common.crypto.TransactionCryptoWrapper;
import io.coti.common.crypto.TransactionTrustScoreCrypto;
import io.coti.common.data.*;
import io.coti.common.http.AddTransactionResponse;
import io.coti.common.http.BaseResponse;
import io.coti.common.http.GetTransactionResponse;
import io.coti.common.http.data.TransactionResponseData;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.Transactions;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IClusterService;
import io.coti.common.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.common.data.TransactionState.*;
import static io.coti.common.http.HttpStringConstants.STATUS_ERROR;
import static io.coti.common.http.HttpStringConstants.TRANSACTION_DOESNT_EXIST_MESSAGE;

@Slf4j
@Service
public class TransactionHelper implements ITransactionHelper {

    @Autowired
    private AddressesTransactionsHistory addressesTransactionsHistory;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private DspConsensusCrypto dspConsensusCrypto;
    @Autowired
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;
    private Map<Hash, Stack<TransactionState>> transactionHashToTransactionStateStackMapping;

    @PostConstruct
    private void init() {
        transactionHashToTransactionStateStackMapping = new ConcurrentHashMap<>();
        log.info("Transaction Helper Started");
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
            AddressTransactionsHistory addressHistory = addressesTransactionsHistory.getByHash(baseTransactionData.getAddressHash());

            if (addressHistory == null) {
                addressHistory = new AddressTransactionsHistory(baseTransactionData.getAddressHash());
            }
            addressHistory.addTransactionHashToHistory(transactionData.getHash());
            addressesTransactionsHistory.put(addressHistory);
        }
    }

    public boolean validateTransaction(TransactionData transactionData) {
        TransactionCryptoWrapper verifyTransaction = new TransactionCryptoWrapper(transactionData);
        return verifyTransaction.isTransactionValid();
    }

    private boolean isTransactionExists(TransactionData transactionData) {
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
        Double transactionTrustScore;
        for (TransactionTrustScoreData transactionTrustScoreData : transactionTrustScores) {
            if (transactionTrustScoreData.getTransactionHash().equals(transactionHash) && transactionTrustScoreCrypto.verifySignature(transactionTrustScoreData)) {
                transactionTrustScore = transactionTrustScoreData.getTrustScore();
                Integer trustScoreResult = trustScoreResults.get(transactionTrustScore);
                trustScoreResults.put(transactionTrustScore, (trustScoreResult != null ? trustScoreResult : 0) + 1);
            }
        }
        if (CollectionUtils.isEmpty(trustScoreResults))
            return false;
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
            log.info("Transaction handled successfully...");
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
        transactions.put(transactionData);
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
        if(!dspConsensusCrypto.verifySignature(dspConsensusResult)){
            log.info("DspConsensus signature verification failed");
            return false;
        }
        TransactionData transactionData = transactions.getByHash(dspConsensusResult.getTransactionHash());
        if(transactionData == null){
            log.info("Transaction doesn't exist");
            return false;
        }
        if (transactionData.getDspConsensusResult() != null) {
            log.info("Transaction vote already exists");
            return false;
        }
        transactionData.setDspConsensusResult(dspConsensusResult);
        if (dspConsensusResult.isDspConsensus()) {
            log.info("Valid vote conclusion received for transaction: {}", dspConsensusResult.getHash());
        } else {
            log.info("Invalid vote conclusion received for transaction: {}", dspConsensusResult.getHash());
        }

        log.info("DspConsensus result for transaction: Hash= {}, DspVoteResult= {}, Index= {}", dspConsensusResult.getHash(), dspConsensusResult.isDspConsensus(), dspConsensusResult.getIndex());
        transactions.put(transactionData);
        return true;
    }
    @Override
    public boolean isConfirmed(TransactionData transactionData) {
        return transactionData.isTrustChainConsensus() && transactionData.getDspConsensusResult() != null && transactionData.getDspConsensusResult().isDspConsensus();
    }
}
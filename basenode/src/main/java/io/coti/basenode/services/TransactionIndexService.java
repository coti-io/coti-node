package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionIndexData;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TransactionIndexService {


    private Map<Hash, Long> transactionsIndexMap;


    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionIndexes transactionIndexes;
    @Autowired
    private Transactions transactions;
    private TransactionIndexData lastTransactionIndexData;


    @PostConstruct
    private void init() {
        transactionsIndexMap = new ConcurrentHashMap<>();
    }

    public synchronized long generateTransactionIndex(TransactionData transactionData) {
        long transactionNextIndex = transactionsIndexMap.size();
        transactionsIndexMap.put(transactionData.getHash(), transactionNextIndex);
        TransactionIndexData transactionIndexData = new TransactionIndexData(transactionData.getHash(), transactionNextIndex,
                getAccumulatedHash(lastTransactionIndexData.getAccumulatedHash(), transactionData.getHash(), lastTransactionIndexData.getIndex() + 1));
        transactionIndexes.put(transactionIndexData);
        return transactionNextIndex;
    }

    public void init(AtomicLong maxTransactionIndex) throws Exception {
        byte[] accumulatedHash = "GENESIS".getBytes();
        TransactionIndexData transactionIndexData = null;
        for (long i = 0; i <= maxTransactionIndex.get(); i++) {
            transactionIndexData = transactionIndexes.getByHash(new Hash(i));
            if (transactionIndexData == null) {
                log.error("Null transaction index data found for index: {}", i);
                throw new Exception(String.format("Null transaction index data found for index: {}", i));
            }

            TransactionData transactionData = transactions.getByHash(transactionIndexData.getTransactionHash());
            if (transactionIndexData == null || transactionData == null) {
                log.error("Null transaction data found for index: {}", i);
                throw new Exception(String.format("Null transaction data found for index: {}", i));
            }
            accumulatedHash = getAccumulatedHash(accumulatedHash, transactionData.getHash(), transactionData.getDspConsensusResult().getIndex());
            if (!Arrays.equals(accumulatedHash, transactionIndexData.getAccumulatedHash())) {
                log.error("Incorrect accumulated hash");
                throw new Exception("Incorrect accumulated hash");
            }
        }
        lastTransactionIndexData = transactionIndexData;
    }

    public synchronized boolean insertNewTransactionIndex(TransactionData transactionData) {
        if (transactionData.getDspConsensusResult() == null) {
            log.error("Invalid transaction index");
            return false;
        }
        if (transactionData.getDspConsensusResult().getIndex() == lastTransactionIndexData.getIndex() + 1) {
            log.debug("Inserting new transaction with index: {}", lastTransactionIndexData.getIndex() + 1);
            lastTransactionIndexData = getNextIndexData(lastTransactionIndexData, transactionData);
            transactionIndexes.put(lastTransactionIndexData);
            transactionHelper.removeNoneIndexedTransaction(transactionData);
        } else {
            log.error("Index is not of the last transaction: Index={}, currentLast={}", transactionData.getDspConsensusResult().getIndex(), lastTransactionIndexData.getIndex());
            return false;
        }
        return true;
    }

    public TransactionIndexData getLastTransactionIndexData() {
        return lastTransactionIndexData;
    }

    public static TransactionIndexData getNextIndexData(TransactionIndexData currentLastTransactionIndexData, TransactionData newTransactionData) {
        return new TransactionIndexData(
                newTransactionData.getHash(),
                currentLastTransactionIndexData.getIndex() + 1,
                getAccumulatedHash(currentLastTransactionIndexData.getAccumulatedHash(), newTransactionData.getHash(), currentLastTransactionIndexData.getIndex() + 1));
    }

    public static byte[] getAccumulatedHash(byte[] previousAccumulatedHash, Hash newTransactionHash, long newIndex) {
        byte[] newTransactionHashBytes = newTransactionHash.getBytes();
        ByteBuffer combinedHash = ByteBuffer.allocate(previousAccumulatedHash.length + newTransactionHashBytes.length + Long.BYTES);
        combinedHash.put(previousAccumulatedHash).put(newTransactionHashBytes).putLong(newIndex);
        return CryptoHelper.cryptoHash(combinedHash.array()).getBytes();
    }

    public Boolean isSynchronized(TransactionIndexData transactionIndexData) {
        TransactionIndexData actualTransactionIndexData = transactionIndexes.getByHash(new Hash(transactionIndexData.getIndex()));
        if (actualTransactionIndexData == null ||
                transactionIndexData.getAccumulatedHash() == null ||
                transactionIndexData.getTransactionHash() == null) {
            return false;
        }

        return transactionIndexData.getIndex() > lastTransactionIndexData.getIndex() - 10 &&
                Arrays.equals(actualTransactionIndexData.getAccumulatedHash(), transactionIndexData.getAccumulatedHash()) &&
                transactionIndexData.getTransactionHash().equals(actualTransactionIndexData.getTransactionHash());
    }
}
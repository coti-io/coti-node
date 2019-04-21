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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TransactionIndexService {
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionIndexes transactionIndexes;
    @Autowired
    private Transactions transactions;
    private TransactionIndexData lastTransactionIndexData;
    private Map<Long, TransactionData> waitingMissingTransactionIndexes = new ConcurrentHashMap<>();

    public void init(AtomicLong maxTransactionIndex) throws Exception {
        log.info("Started to initialize {}", this.getClass().getSimpleName());
        byte[] accumulatedHash = "GENESIS".getBytes();
        TransactionIndexData transactionIndexData = new TransactionIndexData(new Hash(-1), -1, "GENESIS".getBytes());
        TransactionIndexData nextTransactionIndexData;
        try {
            for (long i = 0; i <= maxTransactionIndex.get(); i++) {
                nextTransactionIndexData = transactionIndexes.getByHash(new Hash(i));
                if (nextTransactionIndexData == null) {
                    log.error("Null transaction index data found for index: {}", i);
                    return;
                }

                TransactionData transactionData = transactions.getByHash(nextTransactionIndexData.getTransactionHash());
                if (transactionData == null) {
                    log.error("Null transaction data found for index: {}", i);
                    return;
                }
                accumulatedHash = getAccumulatedHash(accumulatedHash, transactionData.getHash(), transactionData.getDspConsensusResult().getIndex());
                if (!Arrays.equals(accumulatedHash, nextTransactionIndexData.getAccumulatedHash())) {
                    log.error("Incorrect accumulated hash");
                    return;
                }
                transactionIndexData = nextTransactionIndexData;
            }
        } finally {
            lastTransactionIndexData = transactionIndexData;
            log.info("Finished to initialize {}", this.getClass().getSimpleName());
        }
    }

    public synchronized boolean insertNewTransactionIndex(TransactionData transactionData) {
        if (transactionData.getDspConsensusResult() == null) {
            log.error("Invalid transaction index for transaction {}", transactionData.getHash());
            return false;
        }
        if (transactionData.getDspConsensusResult().getIndex() == lastTransactionIndexData.getIndex() + 1) {
            log.debug("Inserting new transaction {} with index: {}", transactionData.getHash(), lastTransactionIndexData.getIndex() + 1);
            lastTransactionIndexData = getNextIndexData(lastTransactionIndexData, transactionData);
            transactionIndexes.put(lastTransactionIndexData);
            transactionHelper.removeNoneIndexedTransaction(transactionData);
        } else {
            //   log.error("Index is not of the last transaction: Index={}, currentLast={}", transactionData.getDspConsensusResult().getIndex(), lastTransactionIndexData.getIndex());
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
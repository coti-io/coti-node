package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionIndexData;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Arrays;

@Slf4j
@Service
public class TransactionIndexService {

    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionIndexes transactionIndexes;
    private TransactionIndexData lastTransactionIndexData;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public synchronized Boolean insertNewTransactionIndex(TransactionData transactionData) {
        if (transactionData.getDspConsensusResult() == null) {
            log.error("Invalid transaction index for transaction {}", transactionData.getHash());
            return null;
        }
        if (transactionData.getDspConsensusResult().getIndex() < lastTransactionIndexData.getIndex() + 1) {
            log.debug("Already inserted index {}", transactionData.getDspConsensusResult().getIndex());
            return null;
        }
        if (transactionData.getDspConsensusResult().getIndex() == lastTransactionIndexData.getIndex() + 1) {
            log.debug("Inserting new transaction {} with index: {}", transactionData.getHash(), lastTransactionIndexData.getIndex() + 1);
            lastTransactionIndexData = getNextIndexData(lastTransactionIndexData, transactionData);
            transactionIndexes.put(lastTransactionIndexData);
            transactionHelper.removeNoneIndexedTransaction(transactionData);
            return true;
        } else {
            return false;
        }
    }

    public TransactionIndexData getLastTransactionIndexData() {
        return lastTransactionIndexData;
    }

    public void setLastTransactionIndexData(TransactionIndexData transactionIndexData) {
        lastTransactionIndexData = transactionIndexData;
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
package io.coti.basenode.crypto;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.InvalidTransactionData;
import io.coti.basenode.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
@Component
public class InvalidTransactionCrypto extends SignatureCrypto<InvalidTransactionData> {

    private static final int BASE_TRANSACTION_HASH_SIZE = 32;

    private byte[] getBaseTransactionsHashesBytes(TransactionData transactionData) {
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        ByteBuffer baseTransactionHashBuffer = ByteBuffer.allocate(baseTransactions.size() * BASE_TRANSACTION_HASH_SIZE);
        baseTransactions.forEach(baseTransaction -> {
            byte[] baseTransactionHashBytes = baseTransaction.getHash().getBytes();
            baseTransactionHashBuffer.put(baseTransactionHashBytes);
        });
        return baseTransactionHashBuffer.array();
    }

    public Hash getHashFromBaseTransactionHashesData(TransactionData transactionData) {
        byte[] bytesToHash = getBaseTransactionsHashesBytes(transactionData);
        return CryptoHelper.cryptoHash(bytesToHash);
    }


    public void setTransactionHash(TransactionData transactionData) {
        try {
            for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
                if (baseTransactionData.getHash() == null) {
                    BaseTransactionCrypto.getByBaseTransactionClass(baseTransactionData.getClass()).setBaseTransactionHash(baseTransactionData);
                }
            }

            Hash transactionHash = getHashFromBaseTransactionHashesData(transactionData);
            transactionData.setHash(transactionHash);
        } catch (Exception e) {
            log.error("Transaction crypto set transaction hash error", e);
        }

    }

    private boolean isTransactionHashCorrect(TransactionData transactionData) {

        Hash generatedTransactionHashFromBaseTransactions = getHashFromBaseTransactionHashesData(transactionData);
        Hash transactionHashFromData = transactionData.getHash();

        return generatedTransactionHashFromBaseTransactions.equals(transactionHashFromData);
    }

    public boolean isTransactionValid(TransactionData transactionData) {

        if (!this.isTransactionHashCorrect(transactionData))
            return false;
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if (!BaseTransactionCrypto.getByBaseTransactionClass(baseTransactionData.getClass()).isBaseTransactionValid(transactionData, baseTransactionData))
                return false;
        }
        return true;
    }

    @Override
    public byte[] getSignatureMessage(InvalidTransactionData invalidTransactionData) {
        byte[] invalidTransactionHashInBytes = invalidTransactionData.getHash().getBytes();
        byte[] invalidationTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(invalidTransactionData.getInvalidationTime().toEpochMilli()).array();
        ByteBuffer transactionBuffer = ByteBuffer.allocate(invalidTransactionHashInBytes.length + invalidationTimeInBytes.length)
                .put(invalidTransactionHashInBytes).put(invalidationTimeInBytes);
        return CryptoHelper.cryptoHash(transactionBuffer.array()).getBytes();
    }
}

package io.coti.basenode.crypto;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;

@Service
public class TransactionCrypto extends SignatureCrypto<TransactionData> {
    final static int baseTransactionHashSize = 32;

    @Override
    public byte[] getMessageInBytes(TransactionData transactionData) {
        return transactionData.getHash().getBytes();
    }

    private byte[] getBaseTransactionsHashesBytes(TransactionData transactionData) {
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        ByteBuffer baseTransactionHashBuffer = ByteBuffer.allocate(baseTransactions.size() * baseTransactionHashSize);
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


        for (BaseTransactionCryptoWrapper bxDataCrypto : transactionData.getBaseTransactions()) {
            if (bxDataCrypto.getBaseTransactionHash() == null)
                bxDataCrypto.setBaseTransactionHash();

        }
        Hash transactionHash = getHashFromBaseTransactionHashesData(transactionData);
        transactionData.setHash(transactionHash);


    }

    private boolean isTransactionHashCorrect(TransactionData transactionData) {

        Hash generatedTxHashFromBaseTransactions = getHashFromBaseTransactionHashesData(transactionData);
        Hash txHashFromData = transactionData.getHash();

        return generatedTxHashFromBaseTransactions.equals(txHashFromData);
    }

    public boolean isTransactionValid(TransactionData transactionData) {

        if (!this.isTransactionHashCorrect(transactionData))
            return false;
        for (BaseTransactionCryptoWrapper bxCrypto : transactionData.getBaseTransactions()) {
            if (bxCrypto.IsBaseTransactionValid(transactionData.getHash()) == false)
                return false;
        }
        return true;
    }


}

package io.coti.basenode.crypto;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TransactionCryptoWrapper {
    final static int baseTransactionHashSize = 32;
    ArrayList<BaseTransactionCryptoWrapper> baseTransactions = new ArrayList<>();
    TransactionData transactionData;

    public TransactionCryptoWrapper(TransactionData transactionData) {
        this.transactionData = transactionData;
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            baseTransactions.add(new BaseTransactionCryptoWrapper(baseTransactionData));
        }
    }

    private byte[] getBaseTransactionsHashesBytes() {
        ByteBuffer baseTransactionHashBuffer = ByteBuffer.allocate(baseTransactions.size() * baseTransactionHashSize);
        for (BaseTransactionCryptoWrapper bxCrypto : this.baseTransactions) {
            byte[] baseTransactionHashBytes = bxCrypto.getBaseTransactionHash().getBytes();
            baseTransactionHashBuffer = baseTransactionHashBuffer.put(baseTransactionHashBytes);
        }
        return baseTransactionHashBuffer.array();
    }

    public Hash getHashFromBaseTransactionHashesData() {
        byte[] bytesToHash = getBaseTransactionsHashesBytes();
        return CryptoHelper.cryptoHash(bytesToHash);
    }


    public void setTransactionHash() {


        for (BaseTransactionCryptoWrapper bxDataCrypto : this.baseTransactions) {
            if (bxDataCrypto.getBaseTransactionHash() == null)
                bxDataCrypto.setBaseTransactionHash();

        }
        Hash transactionHash = getHashFromBaseTransactionHashesData();
        transactionData.setHash(transactionHash);


    }

    private boolean IsTransactionHashCorrect() {

        Hash generatedTxHashFromBaseTransactions = getHashFromBaseTransactionHashesData();
        Hash txHashFromData = this.transactionData.getHash();

        return generatedTxHashFromBaseTransactions.equals(txHashFromData);
    }

    public boolean isTransactionValid() {

        if (!this.IsTransactionHashCorrect())
            return false;
        for (BaseTransactionCryptoWrapper bxCrypto : this.baseTransactions) {
            if (bxCrypto.IsBaseTransactionValid(transactionData.getHash()) == false)
                return false;
        }
        return true;
    }
}

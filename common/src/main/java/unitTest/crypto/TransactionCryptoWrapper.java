package unitTest.crypto;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TransactionCryptoWrapper {
    final static int baseTransactionHashSize = 32;
    ArrayList<BaseTransactionCryptoWrapper> baseTransactions = new ArrayList<>();
    TransactionData txData;

    public TransactionCryptoWrapper(TransactionData txData) {
        this.txData = txData;
        for (BaseTransactionData bxData : txData.getBaseTransactions()) {
            baseTransactions.add(new BaseTransactionCryptoWrapper(bxData));
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
        txData.setHash(transactionHash);


    }

    private boolean IsTransactionHashCorrect() {

        Hash generatedTxHashFromBaseTransactions = getHashFromBaseTransactionHashesData();
        Hash txHashFromData = this.txData.getHash();

        return generatedTxHashFromBaseTransactions.equals(txHashFromData);
    }

    public boolean isTransactionValid() {

        if (!this.IsTransactionHashCorrect())
            return false;
        for (BaseTransactionCryptoWrapper bxCrypto : this.baseTransactions) {
            if (bxCrypto.IsBaseTransactionValid(txData.getHash()) == false)
                return false;
        }
        return true;
    }
}

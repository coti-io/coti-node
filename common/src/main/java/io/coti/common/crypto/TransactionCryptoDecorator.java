package io.coti.common.crypto;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TransactionCryptoDecorator {
     final static int  baseTransactionHashSize = 32;
    ArrayList<BasicTransactionCryptoDecorator> baseTransactions = new ArrayList<BasicTransactionCryptoDecorator>();
    TransactionData txData;

    public TransactionCryptoDecorator(TransactionData txData)
    {
        this.txData = txData;
        for (BaseTransactionData bxData: txData.getBaseTransactions())
        {
            baseTransactions.add(new BasicTransactionCryptoDecorator(bxData,txData.getHash()));
        }
    }

    public TransactionCryptoDecorator(List<BaseTransactionData> basicTransaction, Hash transactionHash, String transactionDescription)
    {
        this.txData = new TransactionData(basicTransaction,transactionHash,transactionDescription);
        for (BaseTransactionData bxData: txData.getBaseTransactions())
        {
            baseTransactions.add(new BasicTransactionCryptoDecorator(bxData,txData.getHash()));
        }
    }



    private byte[] getBaseTransactionsHashesBytes()
    {
        ByteBuffer baseTransactionHashBuffer = ByteBuffer.allocate(baseTransactions.size() * baseTransactionHashSize);
        for (BasicTransactionCryptoDecorator bxCrypto: this.baseTransactions) {
            byte[] baseTransactionHashBytes = bxCrypto.getBasicTransactionHash().getBytes();
            baseTransactionHashBuffer = baseTransactionHashBuffer.put(baseTransactionHashBytes);
        }
        return baseTransactionHashBuffer.array();
    }

    public String getHashFromBasicTransactionHashesData(){
        Keccak.Digest256 digest = new Keccak.Digest256();
        byte[] bytesToHash = getBaseTransactionsHashesBytes();
        digest.update(bytesToHash);
        byte[] digestedHash = digest.digest();
        String hash =   Hex.toHexString( digestedHash).toUpperCase();
        return hash;
    }



    private boolean IsTransacHashCorrect()
    {

        String generatedTxHashFromBaseTransactions = getHashFromBasicTransactionHashesData().toUpperCase();
        String txHashFromData = this.txData.getHash().toHexString().toUpperCase();

        return generatedTxHashFromBaseTransactions.equals(txHashFromData);
    }

    public boolean isTransactionValid() {
        for (BasicTransactionCryptoDecorator bxCrypto: this.baseTransactions) {
            if (bxCrypto.IsBasicTransactionValid() ==false)
                    return false;
        }
        return this.IsTransacHashCorrect();
    }


}
